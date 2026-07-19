# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/client-flying/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/client-flying/README/README_ZH-CN.html)

# Client Flying

A lightweight **client-side Fabric mod** that enables controlled flight in Survival and Adventure modes while avoiding Elytra conflicts and operating entirely on the client.

## Overview

Client Flying allows players to toggle flight in Survival and Adventure modes when they are not wearing Elytra.

The mod also forces the client to continuously report grounded movement to the server and enables local invulnerability flags. All behavior is handled entirely on the client side.

When Elytra is equipped, flight permission is automatically disabled to avoid interfering with vanilla gliding mechanics.

## Key Features

- Enables flight in Survival and Adventure modes
- Automatically disables flight when wearing Elytra
- Continuously synchronizes client abilities
- Forces grounded movement packets
- No commands or configuration required
- Lightweight with minimal performance impact

## Visual Behavior

- Flight becomes available in Survival and Adventure modes
- Wearing Elytra automatically disables custom flight
- Movement packets are sent every tick with grounded state
- No visual UI or overlay is added

## Multiplayer Safety

- Client-side only
- No server modifications required
- Movement packets are manually sent each tick
- Server authority still applies
- May cause desynchronization on servers with strict anti-cheat checks

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
