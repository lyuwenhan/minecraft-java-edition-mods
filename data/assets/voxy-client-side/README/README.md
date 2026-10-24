# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/voxy-client-side/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/voxy-client-side/README/README_ZH-CN.html)

# Voxy Client Side

A client-side Fabric mod that stores one world seed per multiplayer server address and provides the `/voxygen` command.

## Commands

```text
/voxygen on
/voxygen off
/voxygen status
/voxygen toggle
/voxygen radius
/voxygen radius <radius>
/voxygen seed
/voxygen seed <seed>
```

- `on` and `off` enable or disable Voxygen globally.
- `status` shows the current state and the seed recorded for the current world.
- `toggle` switches the current state.
- `radius` shows the configured radius in chunks.
- `radius <radius>` sets the radius to a positive integer. The default is `64` chunks.
- `seed` shows the seed recorded for the current world.
- `seed <seed>` manually sets the seed when no local integrated-server seed is available. The argument is hidden when the local seed can be read directly.

In singleplayer, Voxygen reads the real seed directly from Minecraft's integrated server and uses it without saving it to the configuration. Otherwise, the seed is configured explicitly with `/voxygen seed <seed>`. Voxygen never runs vanilla `/seed` automatically and does not capture its output.

The configuration is saved to `config/voxy-client-side.json`. It contains the global `enable` setting, the generation `radius`, and a `seeds` map keyed by multiplayer server address or singleplayer save path.

## Generation

While enabled, Voxygen visits the square around the player in Chebyshev-distance order and generates missing chunks on background worker threads. It uses Minecraft's local vanilla `ChunkGenerator` through `ChunkStatus.FEATURES`, then reuses Voxy's voxelization, mip generation, and world-update path.

Voxygen skips client-loaded chunks and chunks that already contain Voxy data. It checks again before voxelization and immediately before insertion so generated data does not replace real server terrain.

Generation is available only for locally recognized vanilla Overworld, Nether, and End dimensions. It pauses instead of guessing when the current server has no recorded seed, the dimension is unsupported, Voxy is unavailable, or the required local world-generation registries are missing. Custom server-side world-generation settings cannot be inferred from a seed alone.

## Supported Versions

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25

## License

MIT
