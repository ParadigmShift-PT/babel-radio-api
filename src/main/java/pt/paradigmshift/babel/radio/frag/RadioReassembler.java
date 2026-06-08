package pt.paradigmshift.babel.radio.frag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Transparent radio reassembly — receive side, the counterpart of
 * {@link RadioFragmenter}.
 *
 * <p>Feed every inbound radio frame payload to {@link #offer(Object, byte[])}:
 * <ul>
 *   <li>a non-fragment frame (no {@link RadioFragmenter#FRAGMENT_PROTO}
 *   sentinel) is returned immediately — pass-through, the common path;</li>
 *   <li>a fragment is buffered, keyed by {@code (source, msgId)}, until all
 *   fragments of its message have arrived, then the reassembled enveloped
 *   payload is returned;</li>
 *   <li>an as-yet-incomplete message returns {@link Optional#empty()}.</li>
 * </ul>
 *
 * <p>Partial messages are evicted after a timeout (a lost fragment then forces
 * the sender's higher layer to retransmit the whole message — this layer is
 * best-effort and does not request individual fragments). Eviction is lazy, on
 * each {@code offer}; the number of concurrent in-flight messages is bounded.
 * Out-of-order and duplicate fragments are handled.
 *
 * <p><b>Threading:</b> a single radio has one receive thread, so offers for a
 * given reassembler are serial; {@code offer} is nonetheless
 * {@code synchronized} so an external sweep or inspection thread is safe.
 */
public final class RadioReassembler {

    /** Default partial-message lifetime; well under the sensor-link latency
     *  budget so a lost fragment is abandoned quickly. */
    public static final long DEFAULT_TIMEOUT_MILLIS = 3000;

    /** Bound on concurrently-reassembling messages (oldest is dropped when
     *  exceeded). */
    private static final int MAX_CONCURRENT = 32;

    private record Key(Object src, int msgId) {}

    private static final class Partial {
        final byte[][] chunks;
        final int count;
        int have;
        final long firstSeenMillis;

        Partial(int count, long now) {
            this.chunks = new byte[count][];
            this.count = count;
            this.firstSeenMillis = now;
        }
    }

    private final long timeoutMillis;
    private final Map<Key, Partial> partials = new HashMap<>();

    public RadioReassembler() {
        this(DEFAULT_TIMEOUT_MILLIS);
    }

    public RadioReassembler(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    /**
     * Offers one inbound radio frame payload to the reassembler.
     *
     * @param src          the source identity used to group fragments (the
     *                     sender's radio address — anything with sane
     *                     {@code equals}/{@code hashCode})
     * @param framePayload the radio-payload bytes of the frame
     * @return the complete enveloped payload (the bytes the sender passed to
     *         {@link RadioFragmenter}, i.e. [destProto][user payload]) when the
     *         message is complete, otherwise {@link Optional#empty()}
     */
    public synchronized Optional<byte[]> offer(Object src, byte[] framePayload) {
        long now = System.currentTimeMillis();
        evictExpired(now);

        if (!RadioFragmenter.isFragment(framePayload)) {
            // Non-fragmented message — already complete, hand it straight back.
            return Optional.ofNullable(framePayload);
        }
        if (framePayload.length < RadioFragmenter.FRAGMENT_HEADER_BYTES) {
            return Optional.empty(); // runt fragment header — drop
        }

        int msgId = framePayload[2] & 0xFF;
        int index = framePayload[3] & 0xFF;
        int count = framePayload[4] & 0xFF;
        if (count == 0 || count > RadioFragmenter.MAX_FRAGMENTS
                || index >= count) {
            return Optional.empty(); // malformed header — drop
        }

        Key key = new Key(src, msgId);
        Partial p = partials.get(key);
        if (p == null || p.count != count) {
            // New message (or a stale collision under a reused msgId) — start fresh.
            if (partials.size() >= MAX_CONCURRENT) {
                evictOldest();
            }
            p = new Partial(count, now);
            partials.put(key, p);
        }

        int chunkLen = framePayload.length - RadioFragmenter.FRAGMENT_HEADER_BYTES;
        if (p.chunks[index] == null) {
            byte[] chunk = new byte[chunkLen];
            System.arraycopy(framePayload, RadioFragmenter.FRAGMENT_HEADER_BYTES,
                             chunk, 0, chunkLen);
            p.chunks[index] = chunk;
            p.have++;
        }
        if (p.have < p.count) {
            return Optional.empty();
        }

        partials.remove(key);
        int total = 0;
        for (byte[] c : p.chunks) {
            total += c.length;
        }
        byte[] out = new byte[total];
        int off = 0;
        for (byte[] c : p.chunks) {
            System.arraycopy(c, 0, out, off, c.length);
            off += c.length;
        }
        return Optional.of(out);
    }

    /** Number of partially-reassembled messages currently buffered. */
    public synchronized int pendingCount() {
        return partials.size();
    }

    private void evictExpired(long now) {
        partials.entrySet().removeIf(
                e -> now - e.getValue().firstSeenMillis > timeoutMillis);
    }

    private void evictOldest() {
        Key oldest = null;
        long earliest = Long.MAX_VALUE;
        for (Map.Entry<Key, Partial> e : partials.entrySet()) {
            if (e.getValue().firstSeenMillis < earliest) {
                earliest = e.getValue().firstSeenMillis;
                oldest = e.getKey();
            }
        }
        if (oldest != null) {
            partials.remove(oldest);
        }
    }
}
