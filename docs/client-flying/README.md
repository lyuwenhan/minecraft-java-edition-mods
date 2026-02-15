# Client Flying

A lightweight **client-side Fabric mod** that enables controlled flight behavior in Survival and Adventure modes while preventing Elytra conflicts, operating entirely on the client.

---

## Overview

Client Flying allows players to toggle flight in Survival and Adventure modes when not wearing Elytra.  

The mod also forces the client to continuously report grounded movement to the server and enables local invulnerability flags. All changes are handled entirely on the client side.

When Elytra is equipped, flight permission is automatically disabled to avoid interference with vanilla glide mechanics.

---

## Key Features

- Enables flight in Survival and Adventure modes
- Automatically disables flight when wearing Elytra
- Continuously synchronizes client abilities
- Forces grounded movement packets
- No commands or configuration required
- Lightweight with minimal performance impact

---

## Visual Behavior

- Flight becomes available in Survival and Adventure modes
- Elytra usage automatically disables custom flight
- Movement packets are sent every tick with grounded state
- No visual UI changes or overlays

---

## Multiplayer Safety

- Client-side only
- No server modifications required
- Movement packets are manually sent each tick
- Server authority still applies
- May cause desynchronization on strict anti-cheat servers

---

## Supported Versions

- Minecraft 1.21.11
- Fabric Loader 0.18+
- Fabric API
- Java 21

---

## License

MIT
