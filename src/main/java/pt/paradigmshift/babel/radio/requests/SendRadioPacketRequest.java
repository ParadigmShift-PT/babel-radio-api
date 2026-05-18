package pt.paradigmshift.babel.radio.requests;

import pt.paradigmshift.babel.radio.RadioAddress;
import pt.unl.fct.di.novasys.babel.generic.ProtoRequest;

/**
 * Asks a radio Babel protocol to unicast a payload to a specific peer.
 *
 * <p>The sender stamps the request with its own {@code sourceProto} (its
 * Babel {@code PROTOCOL_ID}) so receivers can demultiplex shared-radio
 * traffic — the radio protocol carries those two bytes on the wire and
 * surfaces them again on
 * {@link pt.paradigmshift.babel.radio.notifications.RadioPacketReceivedNotification}.
 *
 * <p>Routing: send this request to {@code destination.owningProtocolId()} —
 * the address knows which radio protocol owns it.
 */
public class SendRadioPacketRequest extends ProtoRequest {

    public static final short REQUEST_ID = 100;

    private final short sourceProto;
    private final RadioAddress destination;
    private final byte[] payload;

    /**
     * @param sourceProto numeric ID of the sending protocol (its
     *                    {@code PROTOCOL_ID})
     * @param destination peer to receive the payload
     * @param payload     bytes to transmit; must fit within the destination
     *                    radio's MTU (which depends on the concrete radio
     *                    protocol — see its constants)
     */
    public SendRadioPacketRequest(short sourceProto, RadioAddress destination,
                                  byte[] payload) {
        super(REQUEST_ID);
        this.sourceProto = sourceProto;
        this.destination = destination;
        this.payload = payload;
    }

    public short getSourceProto() { return sourceProto; }

    public RadioAddress getDestination() { return destination; }

    public byte[] getPayload() { return payload; }
}
