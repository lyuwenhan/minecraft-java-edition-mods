# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/infinity-fireworks/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/infinity-fireworks/README/README_ZH-CN.html)

# Infinity Fireworks

Fabric mod for Minecraft that lets firework rockets be used without consuming the item stack.

## Features

- Firework rockets are not consumed when launched from a block
- Firework rockets are not consumed when used for elytra boosting
- Works in singleplayer through the integrated server
- Works on Fabric servers when installed server-side
- Keeps vanilla firework behavior, including flight data and explosions
- No commands or configuration required

## Usage Example

```text
Use a firework rocket -> rocket launches -> item count stays the same
```

Field description:

- `Use a firework rocket` - Right-click a block or boost while gliding
- `rocket launches` - Vanilla firework behavior still happens
- `item count stays the same` - The stack is not decremented

## Behavior

- Applies to all firework rockets, including customized rockets
- Does not create new items or change crafting recipes
- Does not affect dispenser behavior
- Creative mode behavior remains unchanged

## Multiplayer Safety

- Client-only installation does not change item consumption on remote servers
- Multiplayer servers must install the mod server-side for the effect to apply
- When installed on a server, the non-consumption behavior applies to players using that server
- The mod does not register commands or send custom gameplay packets

## Supported Versions

- Minecraft 1.21.11
- Fabric Loader 0.18+
- Fabric API
- Java 21

## License

MIT
