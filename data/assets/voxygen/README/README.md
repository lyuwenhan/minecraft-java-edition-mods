# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/voxygen/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/voxygen/README/README_ZH-CN.html)

# Voxygen

A client-side Fabric mod that generates missing vanilla chunks into Voxy using per-server seeds and client-side commands.

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
/voxygen progress
/voxygen progress on|off|toggle
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
- `progress` shows whether the generation progress boss bar is enabled.
- `progress on|off|toggle` controls the yellow `Voxygen` progress boss bar. It is disabled by default and disappears two seconds after generation completes.
- `seed` shows the seed recorded for the current world.
- `seed <seed>` manually sets the seed when no local integrated-server seed is available. The argument is hidden when the local seed can be read directly.

In singleplayer, Voxygen reads the real seed directly from Minecraft's integrated server and uses it without saving it to the configuration. Otherwise, the seed is configured explicitly with `/voxygen seed <seed>`.

## Supported Versions

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25
- Voxy (Required)

## License

MIT
