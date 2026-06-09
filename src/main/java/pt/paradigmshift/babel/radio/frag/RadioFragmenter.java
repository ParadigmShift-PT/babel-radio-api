package pt.paradigmshift.babel.radio.frag;

import java.util.ArrayList;
import java.util.List;

/**
 * Transparent radio fragmentation — transmit side.
 *
 * <p>Splits an already-enveloped radio payload that does not fit a single
 * on-air frame into a sequence of fragment frames, each small enough for one
 * frame. The matching {@link RadioReassembler} rebuilds the original bytes on
 * the far side, so the protocols above {@code babel-radio-api} hand off
 * arbitrarily-sized payloads and never see fragments.
 *
 * <p>This is a transport concern that lives <em>below</em> the
 * {@code destProto} envelope: it operates on opaque already-enveloped bytes
 * and never inspects or rewrites the destProto (the radio protocol still packs
 * and unpacks those two bytes inline, per the {@code babel-radio-api}
 * convention). A fragment frame is marked by the reserved {@link
 * #FRAGMENT_PROTO} sentinel in the two bytes where a normal frame carries its
 * destProto, so a message that already fits one frame is sent verbatim — the
 * common path pays zero overhead and is byte-for-byte identical to a build
 * without fragmentation.
 *
 * <h2>Fragment frame layout (radio payload)</h2>
 * <pre>
 *   [ FRAGMENT_PROTO : u16 BE ][ msgId : u8 ][ index : u8 ][ count : u8 ][ chunk ... ]
 * </pre>
 * Concatenating {@code chunk} over {@code index = 0 .. count-1} reconstructs
 * the original enveloped payload ([destProto][user payload]). The reassembled
 * message's integrity is covered end-to-end by the application's own
 * authentication (e.g. the sensor-link AEAD tag), so no checksum is added
 * here; each on-air frame is already CRC-protected by the radio PHY.
 */
public final class RadioFragmenter {

    /**
     * Reserved {@code destProto} sentinel marking a fragment frame, written
     * big-endian in the two bytes a normal frame uses for its destProto. No
     * Babel protocol may use this id on the radio.
     */
    public static final short FRAGMENT_PROTO = (short) 0xFFFE;

    /** Per-fragment framing overhead: 2 (sentinel) + msgId + index + count. */
    public static final int FRAGMENT_HEADER_BYTES = 5;

    /** Hard cap on fragments per message — bounds reassembly memory. */
    public static final int MAX_FRAGMENTS = 16;

    private RadioFragmenter() {}

    /**
     * Splits {@code enveloped} into frames that each fit {@code frameCapacity}
     * bytes of radio payload.
     *
     * @param enveloped     the bytes to send ([destProto][user payload])
     * @param frameCapacity max radio-payload bytes per frame (e.g. 229 on LoRa,
     *                      121 on ZigBee)
     * @param msgId         per-sender rolling id grouping these fragments
     *                      (only the low 8 bits are used)
     * @return one {@code byte[]} per frame to hand to the driver; a
     *         single-element list holding {@code enveloped} verbatim when it
     *         already fits one frame
     * @throws IllegalArgumentException if the message would need more than
     *         {@link #MAX_FRAGMENTS} fragments, or {@code frameCapacity} is too
     *         small to carry a fragment header
     */
    public static List<byte[]> fragment(byte[] enveloped, int frameCapacity,
                                        int msgId) {
        if (enveloped.length <= frameCapacity) {
            List<byte[]> single = new ArrayList<>(1);
            single.add(enveloped);
            return single;
        }
        int chunkCap = frameCapacity - FRAGMENT_HEADER_BYTES;
        if (chunkCap <= 0) {
            throw new IllegalArgumentException(
                    "frameCapacity " + frameCapacity
                            + "B too small for a " + FRAGMENT_HEADER_BYTES
                            + "B fragment header");
        }
        int count = (enveloped.length + chunkCap - 1) / chunkCap;
        if (count > MAX_FRAGMENTS) {
            throw new IllegalArgumentException(
                    "message of " + enveloped.length + "B needs " + count
                            + " fragments (max " + MAX_FRAGMENTS + ")");
        }
        List<byte[]> frames = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int off = i * chunkCap;
            int len = Math.min(chunkCap, enveloped.length - off);
            byte[] frame = new byte[FRAGMENT_HEADER_BYTES + len];
            frame[0] = (byte) ((FRAGMENT_PROTO >> 8) & 0xFF);
            frame[1] = (byte) (FRAGMENT_PROTO & 0xFF);
            frame[2] = (byte) (msgId & 0xFF);
            frame[3] = (byte) i;
            frame[4] = (byte) count;
            System.arraycopy(enveloped, off, frame, FRAGMENT_HEADER_BYTES, len);
            frames.add(frame);
        }
        return frames;
    }

    /**
     * @return {@code true} if {@code framePayload} is a fragment frame (its
     *         leading two bytes carry the {@link #FRAGMENT_PROTO} sentinel)
     */
    public static boolean isFragment(byte[] framePayload) {
        return framePayload != null && framePayload.length >= 2
                && framePayload[0] == (byte) ((FRAGMENT_PROTO >> 8) & 0xFF)
                && framePayload[1] == (byte) (FRAGMENT_PROTO & 0xFF);
    }
}
