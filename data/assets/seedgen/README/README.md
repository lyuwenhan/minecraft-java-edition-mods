# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/voxygen/README/README.html)|[ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/voxygen/README/README_ZH-CN.html)

# Voxygen

A client-side Fabric mod that generates vanilla terrain locally and submits it to [Voxy](https://modrinth.com/mod/voxy). It fills distant Voxy terrain without asking the server to load or generate every source chunk.

> [!IMPORTANT]
> Multiplayer generation requires the exact world seed. A wrong seed produces terrain that does not match the server. Due to [MC-55596](https://bugs.mojang.com/browse/MC/issues/MC-55596), generated features such as trees may be inconsistent with the server even when the seed is correct.

## Commands

|Command|Description|
|---|---|
|`/voxygen on`|Enable Voxygen.|
|`/voxygen off`|Disable Voxygen and stop the active session.|
|`/voxygen toggle`|Toggle Voxygen.|
|`/voxygen status`|Show the world, seed, radius, progress, thread usage, and result counters.|
|`/voxygen radius`|Show the configured radius in vanilla chunks.|
|`/voxygen radius <radius>`|Set the radius. Default: `64`; minimum: `1`.|
|`/voxygen threads`|Show the worker-thread count.|
|`/voxygen threads <threads>`|Set worker threads from `1` to `256`. Default: `32`.|
|`/voxygen progress`|Show whether the progress boss bar is enabled.|
|`/voxygen progress on\|off\|toggle`|Control the progress boss bar. It is disabled by default.|
|`/voxygen seed`|Show the effective seed for the current world.|
|`/voxygen seed <seed>`|Set the multiplayer seed. Hidden when the integrated server provides it.|

## Performance notes

- Chunk generation is CPU- and memory-intensive. Increase `threads` gradually rather than immediately using every available logical processor.
- A larger radius increases the number of large regions quadratically.

## Supported Versions

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25
- Voxy (Required)

## License

MIT
