package pt.paradigmshift.babel.radio;

/**
 * Protocol-agnostic peer identifier for radio Babel protocols.
 *
 * <p>Concrete radio protocols (LoRa, ZigBee, …) carry different on-air address
 * types — a 16-bit numeric address for LoRa, a 64-bit IEEE EUI for ZigBee,
 * and so on. This abstract base lets application protocols hold and compare
 * peer identities without knowing which radio they belong to, while still
 * routing each request to the right protocol via
 * {@link #owningProtocolId()}.
 *
 * <h2>Equality</h2>
 * Two {@code RadioAddress} instances are equal iff they are the same concrete
 * subclass and {@link #key()} compares equal. Cross-subclass comparison
 * always returns {@code false} — a {@code LoRaAddress} never equals a
 * {@code ZigBeeAddress}, even if the underlying numeric bits happened to
 * coincide. Subclasses cannot override the comparison logic; they only
 * supply the identity key.
 *
 * <h2>Routing</h2>
 * Application code that holds a {@code RadioAddress} and wants to direct a
 * request to the correct Babel protocol can use
 * {@link #owningProtocolId()} without case-analysis:
 *
 * <pre>{@code
 * sendRequest(new SendRadioPacketRequest(MY_PROTOCOL_ID, addr, payload),
 *             addr.owningProtocolId());
 * }</pre>
 */
public abstract class RadioAddress {

    /**
     * The class-specific identity used by {@link #equals(Object)} and
     * {@link #hashCode()}. Must be a stable, value-equal object — typically
     * a boxed primitive, a {@code String}, or an immutable value type.
     */
    protected abstract Object key();

    /**
     * Numeric ID of the Babel protocol that owns this address type. Used by
     * application code to route requests to the right radio protocol without
     * pattern-matching on the concrete subclass.
     */
    public abstract short owningProtocolId();

    @Override
    public final boolean equals(Object o) {
        return o != null && o.getClass() == this.getClass()
                && key().equals(((RadioAddress) o).key());
    }

    @Override
    public final int hashCode() {
        return key().hashCode();
    }
}
