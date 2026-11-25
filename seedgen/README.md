# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/seedgen/README/README.html)|[ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/seedgen/README/README_ZH-CN.html)

# Seedgen

A client-side Fabric mod that locally generates vanilla terrain from a known world seed without asking the server to load or generate every source chunk.

Supported mods: [Voxy](https://modrinth.com/mod/voxy), [Xaero's World Map](https://modrinth.com/mod/xaeros-world-map), [JourneyMap](https://modrinth.com/plugin/journeymap).

> [!IMPORTANT]
> Multiplayer generation requires the exact world seed. A wrong seed produces terrain that does not match the server. Due to [MC-55596](https://bugs.mojang.com/browse/MC/issues/MC-55596), generated features such as trees may be inconsistent with the server even when the seed is correct.
>
> Seeds are stored per server and per world (dimension). On servers that run several worlds with different seeds (for example a lobby, a resource and a building world), stand in each world and set its own seed with `/seedgen seed <seed>`. If a world spans several dimensions, enter the same seed inside each of them; the vanilla Nether and End automatically inherit the seed set for the Overworld.

## Commands

|Command|Description|
|---|---|
|`/seedgen on`|Enable Seedgen.|
|`/seedgen off`|Disable Seedgen and stop the active session.|
|`/seedgen toggle`|Toggle Seedgen.|
|`/seedgen status`|Show the world, seed, radius, progress, thread usage, and result counters.|
|`/seedgen radius`|Show the configured radius in vanilla chunks.|
|`/seedgen radius <radius>`|Set the radius. Default: `64`; minimum: `1`.|
|`/seedgen threads`|Show the worker-thread count.|
|`/seedgen threads <threads>`|Set worker threads from `1` to `256`. Default: `32`.|
|`/seedgen progress`|Show whether the progress boss bar is enabled.|
|`/seedgen progress on\|off\|toggle`|Control the progress boss bar. It is disabled by default.|
|`/seedgen generateforfreecam`|Show whether Freecam target generation is enabled.|
|`/seedgen generateforfreecam on\|off\|toggle`|Control whether to generate around the current camera. Disabled by default.|
|`/seedgen generateforworldmapcenter`|Show whether world-map-center target generation is enabled.|
|`/seedgen generateforworldmapcenter on\|off\|toggle`|Use the center of an open Xaero or JourneyMap full-screen map as a generation target. Disabled by default.|
|`/seedgen seed`|Show the effective seed for the current world.|
|`/seedgen seed <seed>`|Set the multiplayer seed for the current world. Hidden when the integrated server provides it.|

## Supported Versions

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25
- Voxy (Optional)
- Xaero's World Map (Optional)
- JourneyMap (Optional)

## License

MIT
