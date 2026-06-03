package pt.paradigmshift.babel.radio.notifications;

import pt.paradigmshift.babel.radio.RadioAddress;
import pt.unl.fct.di.novasys.babel.generic.ProtoNotification;

/**
 * Delivered when a {@link pt.paradigmshift.babel.radio.requests.SendRadioPacketRequest}
 * or {@link pt.paradigmshift.babel.radio.requests.BroadcastRadioPacketRequest}
 * could not be transmitted. Carries the {@link #getDestProto()} of the failed
 * send (the value the requester passed); a requester recognises its own failed
 * send by matching it, just as receivers filter inbound packets on it.
 *
 * <p>Synchronous failures only: payload exceeded the MTU, destination
 * unknown to the radio, the driver threw on transmit. Asynchronous radio-
 * layer failures (timeouts, NACKs) are not surfaced — concrete radio
 * protocols document any per-radio exceptions.
 *
 * <p>{@link #getDestination()} is {@code null} for broadcast failures,
 * since a broadcast has no single destination.
 *
 * <p><b>Handler class:</b> notification. <b>ID:</b> {@value #NOTIFICATION_ID}
 * — reserved in the {@code babel-radio-api} slot (notional protocol id 400).
 */
public class RadioSendFailedNotification extends ProtoNotification {

    public static final short NOTIFICATION_ID = 402;

    private final short destProto;
    private final RadioAddress destination;
    private final String reason;

    public RadioSendFailedNotification(short destProto,
                                       RadioAddress destination,
                                       String reason) {
        super(NOTIFICATION_ID);
        this.destProto = destProto;
        this.destination = destination;
        this.reason = reason;
    }

    /** Destination protocol id of the failed send (the value the requester passed). */
    public short getDestProto() { return destProto; }

    /** May be {@code null} for broadcast failures. */
    public RadioAddress getDestination() { return destination; }

    public String getReason() { return reason; }
}
