package pt.paradigmshift.babel.radio.notifications;

import pt.paradigmshift.babel.radio.RadioAddress;
import pt.unl.fct.di.novasys.babel.generic.ProtoNotification;

/**
 * Delivered to every protocol that subscribes to it, for every packet
 * received by a radio Babel protocol. Subscribers filter on
 * {@link #getDestProto()} to keep only the traffic addressed to their
 * protocol: the value is the <b>destination protocol id</b> — the id of the
 * local protocol the frame is for. (By Babel's symmetric "protocol N talks
 * to protocol N" convention a remote peer stamps its own {@code PROTOCOL_ID},
 * which on this side is the destination; an asymmetric sender such as a
 * µBabel sensor stamps the gateway protocol's id directly.)
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

    private final short destProto;
    private final RadioAddress origin;
    private final byte[] payload;

    public RadioPacketReceivedNotification(short destProto,
                                           RadioAddress origin,
                                           byte[] payload) {
        super(NOTIFICATION_ID);
        this.destProto = destProto;
        this.origin = origin;
        this.payload = payload;
    }

    /** Destination protocol id: the local Babel protocol this frame is addressed to. */
    public short getDestProto() { return destProto; }

    public RadioAddress getOrigin() { return origin; }

    public byte[] getPayload() { return payload; }
}
