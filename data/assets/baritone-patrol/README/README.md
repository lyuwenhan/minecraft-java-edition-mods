# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/baritone-patrol/README/README.html)|[ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/baritone-patrol/README/README_ZH-CN.html)

# Baritone Patrol

A client-side Fabric mod that records an ordered route and makes Baritone continuously patrol its waypoints in a loop.

## Commands

|Command|Description|
|---|---|
|`/patrol add`|Append the player's current block position to the route.|
|`/patrol add <x> <y> <z>`|Append the specified integer coordinates to the route.|
|`/patrol start`|Start patrolling from the first waypoint.|·
|`/patrol stop`|Stop the active patrol. If no patrol is active, other Baritone goals are left untouched.|
|`/patrol clear`|Delete all recorded waypoints for the current world or server.|
|`/patrol list`|Display the recorded route in order.|

## Requirements

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25
- [Baritone](https://github.com/cabaletta/baritone) (强制要求)

## License

MIT
