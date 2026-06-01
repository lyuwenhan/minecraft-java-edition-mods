# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/better-elytra-takeoff/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/better-elytra-takeoff/README/README_ZH-CN.html)

# Better Elytra Takeoff

A Fabric server-side / single-player mod that lets players start elytra gliding from the ground with a firework rocket.

## Features

- Use a firework rocket to start gliding from the ground
- Immediately applies the firework boost after takeoff
- Works on dedicated servers and single-player worlds
- No commands or config required

## Behavior

When a player uses a firework rocket, the mod cancels the normal use if the takeoff conditions are met, starts elytra gliding, and spawns the boost rocket.

Takeoff requires:

- Holding a firework rocket
- Wearing a glider item, such as an elytra
- Not sneaking
- Not already gliding
- Not spectating, riding, or creative-flying

Aim rule:

- If targeting a block, the player must look horizontally or upward
- If not targeting a block, any look direction can trigger takeoff

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
