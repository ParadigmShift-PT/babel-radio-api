package pt.paradigmshift.babel.radio.requests;

import pt.paradigmshift.babel.radio.RadioAddress;
import pt.unl.fct.di.novasys.babel.generic.ProtoRequest;

/**
 * Asks a radio Babel protocol to unicast a payload to a specific peer.
 *
 * <p>The request carries a {@code destProto} — the id of the protocol on the
 * receiving node that should handle the payload. The radio protocol writes
 * those two bytes on the wire and surfaces them again on
 * {@link pt.paradigmshift.babel.radio.notifications.RadioPacketReceivedNotification#getDestProto()},
 * where receivers filter on it. By Babel's symmetric "protocol N talks to
 * protocol N" convention a protocol passes its own {@code PROTOCOL_ID} here
 * (so it reaches its twin on the peer); an asymmetric link can target any
 * remote protocol id.
 *
 * <p>Routing: send this request to {@code destination.owningProtocolId()} —
 * the address knows which radio protocol owns it.
 *
 * <p><b>Handler class:</b> request/reply. <b>ID:</b> {@value #REQUEST_ID}
 * — reserved in the {@code babel-radio-api} slot (notional protocol id 400).
 */
public class SendRadioPacketRequest extends ProtoRequest {

    public static final short REQUEST_ID = 401;

    private final short destProto;
    private final RadioAddress destination;
    private final byte[] payload;

    /**
     * @param destProto   id of the protocol on the receiving node that should
     *                    handle this payload (by convention the sender's own
     *                    {@code PROTOCOL_ID})
     * @param destination peer to receive the payload
     * @param payload     bytes to transmit; must fit within the destination
     *                    radio's MTU (which depends on the concrete radio
     *                    protocol — see its constants)
     */
    public SendRadioPacketRequest(short destProto, RadioAddress destination,
                                  byte[] payload) {
        super(REQUEST_ID);
        this.destProto = destProto;
        this.destination = destination;
        this.payload = payload;
    }

    public short getDestProto() { return destProto; }

    public RadioAddress getDestination() { return destination; }

    public byte[] getPayload() { return payload; }
}
