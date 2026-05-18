package pt.paradigmshift.babel.radio.notifications;

import pt.paradigmshift.babel.radio.RadioAddress;
import pt.unl.fct.di.novasys.babel.generic.ProtoNotification;

/**
 * Delivered when a {@link pt.paradigmshift.babel.radio.requests.SendRadioPacketRequest}
 * or {@link pt.paradigmshift.babel.radio.requests.BroadcastRadioPacketRequest}
 * could not be transmitted. Subscribers filter on {@link #getSourceProto()}
 * (the requester's {@code PROTOCOL_ID}) just as for inbound packets.
 *
 * <p>Synchronous failures only: payload exceeded the MTU, destination
 * unknown to the radio, the driver threw on transmit. Asynchronous radio-
 * layer failures (timeouts, NACKs) are not surfaced — concrete radio
 * protocols document any per-radio exceptions.
 *
 * <p>{@link #getDestination()} is {@code null} for broadcast failures,
 * since a broadcast has no single destination.
 */
public class RadioSendFailedNotification extends ProtoNotification {

    public static final short NOTIFICATION_ID = 101;

    private final short sourceProto;
    private final RadioAddress destination;
    private final String reason;

    public RadioSendFailedNotification(short sourceProto,
                                       RadioAddress destination,
                                       String reason) {
        super(NOTIFICATION_ID);
        this.sourceProto = sourceProto;
        this.destination = destination;
        this.reason = reason;
    }

    public short getSourceProto() { return sourceProto; }

    /** May be {@code null} for broadcast failures. */
    public RadioAddress getDestination() { return destination; }

    public String getReason() { return reason; }
}
