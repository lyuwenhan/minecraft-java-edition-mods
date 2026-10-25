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
/voxygen threads
/voxygen threads <threads>
/voxygen seed
/voxygen seed <seed>
```

- `on` and `off` enable or disable Voxygen globally.
- `status` shows the current state and the seed recorded for the current world.
- `toggle` switches the current state.
- `radius` shows the configured radius in chunks.
- `radius <radius>` sets the radius to a positive integer. The default is `64` chunks.
- `threads` shows the configured worker count.
- `threads <threads>` sets the worker count from `1` to `256`. The default is `32`.
- `seed` shows the seed recorded for the current world.
- `seed <seed>` manually sets the seed when no local integrated-server seed is available. The argument is hidden when the local seed can be read directly.

In singleplayer, Voxygen reads the real seed directly from Minecraft's integrated server and uses it without saving it to the configuration. Otherwise, the seed is configured explicitly with `/voxygen seed <seed>`. Voxygen never runs vanilla `/seed` automatically and does not capture its output.

The configuration is saved to `config/voxy-client-side.json`. It contains the global `enable` setting, generation `radius`, worker count `threads`, and a `seeds` map keyed by multiplayer server address or singleplayer save path.

## Generation

While enabled, Voxygen visits the square around the player in a continuous clockwise square spiral based on Chebyshev distance. It generates missing chunks on background worker threads, using Minecraft's local vanilla `ChunkGenerator` through `ChunkStatus.TERRAIN`, then reuses Voxy's voxelization, mip generation, and world-update path.

Only the target `1x1` chunk is generated; neighboring chunks are not generated or read. This produces biomes and base terrain, including aquifer fluids, but deliberately excludes trees, structures, ores, vegetation, and placed lakes. Voxelized terrain currently uses sky light `15` and block light `0`.

Voxygen determines whether to skip a chunk solely from data actually stored in Voxy; vanilla client render distance does not affect the decision. It checks Voxy before generation, before voxelization, and immediately before insertion so generated data does not replace terrain already received from the server.

The generation window follows the player. Center and radius updates run on a dedicated background thread, coalesce rapid changes, and rebuild the remaining queue in spiral order while retaining completed, active, and retry work that still overlaps the new window. After one update finishes, another can run after a one-second cooldown. Initial generation starts two seconds after joining a valid world.

Changing the worker count does not restart generation. Excess active jobs finish normally when the count is lowered, while raising it allows more jobs to start. Failed chunks enter a retry queue. When a session must be replaced, it stops dispatching new work and lets active jobs finish and save first.

Generation is available only for locally recognized vanilla Overworld, Nether, and End dimensions. It pauses instead of guessing when the current server has no recorded seed, the dimension is unsupported, Voxy is unavailable, or the required local world-generation registries are missing. Custom server-side world-generation settings cannot be inferred from a seed alone.

## Supported Versions

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25
- Voxy (Required)

## License

MIT
