package pt.paradigmshift.babel.radio.requests;

import pt.unl.fct.di.novasys.babel.generic.ProtoRequest;

/**
 * Asks a radio Babel protocol to broadcast a payload to every peer in
 * one-hop reach.
 *
 * <p>"Broadcast" semantics depend on the radio:
 * <ul>
 *   <li><b>LoRa:</b> destination address {@code 0xFFFF} on the shared
 *   half-duplex medium — every radio in range receives it.</li>
 *   <li><b>ZigBee:</b> the NWK-layer all-devices broadcast address
 *   ({@code 0xFFFF}) — delivered to every joined node, but excludes sleepy
 *   end devices that are not currently awake.</li>
 * </ul>
 *
 * <p>Routing: send to the {@code PROTOCOL_ID} of the radio protocol you
 * want to broadcast through. There is no destination address on a broadcast
 * request — the application chooses the radio explicitly.
 *
 * <p><b>Handler class:</b> request/reply. <b>ID:</b> {@value #REQUEST_ID}
 * — reserved in the {@code babel-radio-api} slot (notional protocol id 400).
 */
public class BroadcastRadioPacketRequest extends ProtoRequest {

    public static final short REQUEST_ID = 402;

    private final short destProto;
    private final byte[] payload;

    /**
     * @param destProto id of the protocol on the receiving nodes that should
     *                  handle this payload (by convention the sender's own
     *                  {@code PROTOCOL_ID})
     * @param payload   bytes to broadcast; must fit within the radio MTU
     */
    public BroadcastRadioPacketRequest(short destProto, byte[] payload) {
        super(REQUEST_ID);
        this.destProto = destProto;
        this.payload = payload;
    }

    public short getDestProto() { return destProto; }

    public byte[] getPayload() { return payload; }
}
