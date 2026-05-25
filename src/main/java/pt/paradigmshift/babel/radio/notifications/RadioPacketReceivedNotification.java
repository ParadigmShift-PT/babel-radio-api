package pt.paradigmshift.babel.radio.notifications;

import pt.paradigmshift.babel.radio.RadioAddress;
import pt.unl.fct.di.novasys.babel.generic.ProtoNotification;

/**
 * Delivered to every protocol that subscribes to it, for every packet
 * received by a radio Babel protocol. Subscribers filter on
 * {@link #getSourceProto()} to keep only the traffic addressed to their
 * protocol — by convention the remote sender stamps its own
 * {@code PROTOCOL_ID} there.
 *
 * <p>This notification is intentionally radio-agnostic: it carries only the
 * minimum fields every radio can supply. Concrete radio protocols may
 * deliver a subclass of this type that adds radio-specific metadata (RSSI,
 * previous hop, application-level packet identifiers). Subscribers that
 * need those extras do an {@code instanceof} cast at the handler level.
 *
 * <p>All subclasses share the same {@link #NOTIFICATION_ID} via
 * {@code super(NOTIFICATION_ID)} — a single subscription captures traffic
 * from every radio protocol on the gateway.
 *
 * <p><b>Handler class:</b> notification. <b>ID:</b> {@value #NOTIFICATION_ID}
 * — reserved in the {@code babel-radio-api} slot (notional protocol id 400).
 */
public class RadioPacketReceivedNotification extends ProtoNotification {

    public static final short NOTIFICATION_ID = 401;

    private final short sourceProto;
    private final RadioAddress origin;
    private final byte[] payload;

    public RadioPacketReceivedNotification(short sourceProto,
                                           RadioAddress origin,
                                           byte[] payload) {
        super(NOTIFICATION_ID);
        this.sourceProto = sourceProto;
        this.origin = origin;
        this.payload = payload;
    }

    public short getSourceProto() { return sourceProto; }

    public RadioAddress getOrigin() { return origin; }

    public byte[] getPayload() { return payload; }
}
