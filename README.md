# Babel Radio API

Shared Babel request / notification surface for radio-style transport protocols
(LoRa, ZigBee, future BLE / sub-GHz / Wi-Fi mesh radios). Defines an abstract
`RadioAddress` and the four request / notification types that a radio Babel
protocol exposes to its applications — so application protocols can be
radio-agnostic.

**Group ID:** `pt.paradigmshift.babel`
**Artifact ID:** `babel-radio-api`
**Current version:** `0.1.0`
**Tested with:** `pt.paradigmshift.babel:babel-core:1.0.0`.
**Consumed by:** `babel-lora-protocol`, `babel-zigbee-protocol`.

---

## API surface

| Type | ID | Purpose |
|---|---|---|
| `SendRadioPacketRequest`         | `100` (request)      | Unicast a payload to a specific `RadioAddress` |
| `BroadcastRadioPacketRequest`    | `101` (request)      | Broadcast a payload to every one-hop peer on a given radio |
| `RadioPacketReceivedNotification`| `100` (notification) | One per received packet — fan-out to every subscriber, filter by `getSourceProto()` |
| `RadioSendFailedNotification`    | `101` (notification) | Synchronous send failure (MTU exceeded, destination unknown, driver throw) |

All IDs are constants on the corresponding class. Babel scopes IDs per
registering protocol, so a `LoRaProtocol` and a `ZigBeeProtocol` registering
handlers for `SendRadioPacketRequest.REQUEST_ID` is not a conflict —
each receives only the requests targeted at its own `PROTOCOL_ID`.

## RadioAddress

A protocol-agnostic peer identifier. Concrete radio protocols subclass it
(`LoRaAddress`, `ZigBeeAddress`, …) to wrap their on-air address types. The
base class enforces:

- **`equals` / `hashCode`** — final, key-based: two addresses are equal iff
  they are the same concrete subclass and the keys returned by
  `key()` compare equal. A `LoRaAddress` never equals a `ZigBeeAddress`,
  even if their numeric values coincide.
- **`owningProtocolId()`** — returns the `PROTOCOL_ID` of the Babel
  protocol that knows how to send to this address. Lets application code
  route correctly without pattern-matching on subclass:

  ```java
  sendRequest(new SendRadioPacketRequest(MY_PROTOCOL_ID, addr, payload),
              addr.owningProtocolId());
  ```

## Multi-radio application code

Because both protocols use the same notification ID, a single subscription
captures inbound traffic from every radio. The local `sourceProto`
parameter Babel passes to the handler tells you which protocol fired it:

```java
subscribeNotification(RadioPacketReceivedNotification.NOTIFICATION_ID,
        (n, localSrc) -> {
    if (n.getSourceProto() != MY_PROTOCOL_ID) return;   // not for our app proto
    if (localSrc == LoRaProtocol.PROTOCOL_ID) {
        // arrived via LoRa
    } else if (localSrc == ZigBeeProtocol.PROTOCOL_ID) {
        // arrived via ZigBee
    }
    handlePeerMessage(n.getOrigin(), n.getPayload());
});
```

Concrete radio protocols may deliver subclasses of
`RadioPacketReceivedNotification` carrying radio-specific extras (LoRa RSSI,
ZigBee `id`/`val`). The shared ID means the subscription above still
receives them; cast to the specific type when you need the extras:

```java
if (n instanceof LoRaPacketReceivedNotification l) {
    int rssi = l.getRssi();
}
```

---

## Usage

Add to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>paradigmshift-repository</id>
        <name>ParadigmShift Repository</name>
        <url>https://maven.paradigmshift.pt/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>pt.paradigmshift.babel</groupId>
        <artifactId>babel-radio-api</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

This artifact pulls in `pt.paradigmshift.babel:babel-core` transitively.

> You typically do not depend on this artifact directly — it is brought in
> as a transitive dependency by `babel-lora-protocol` or
> `babel-zigbee-protocol`. Depend on it explicitly only when writing
> radio-agnostic application code that holds `RadioAddress` references or
> sends `SendRadioPacketRequest` without compile-time coupling to a
> specific radio.

---

## Building

Requires Java 17 and Maven 3.6+.

```bash
mvn verify    # compile + (no) tests
mvn package   # produces JAR, sources JAR, and Javadoc JAR
mvn install   # install to ~/.m2/
mvn deploy    # publish to maven.paradigmshift.pt (requires REPOSILITE_TOKEN)
```

## Releasing

Push a version tag — CI deploys automatically:

```bash
git tag v0.1.0
git push origin v0.1.0
```

---

## License

Copyright (c) 2026 ParadigmShift, Lda. See [LICENSE](LICENSE) for full terms.

Commercial use outside of ParadigmShift requires a written licence.
Contact: [info@paradigmshift.pt](mailto:info@paradigmshift.pt)
