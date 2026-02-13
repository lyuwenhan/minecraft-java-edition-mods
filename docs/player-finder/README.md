# Player Finder

Client-side Fabric HUD mod that displays nearby player information directly on your screen in Minecraft.

## Features

- Displays nearby players in a clean HUD (bottom-left by default)
- Shows player name, direction arrow, distance, health, and coordinates
- Direction arrow is relative to your current view
- Real-time updates while moving or turning
- Client-only, safe for multiplayer servers
- No server installation required

## HUD Example

```
Player867 ↑ 11m ❤ 20.0 (123, 64, -456)
```

Field description:

- `Player867` — Target player name  
- `↑` — Direction relative to your current facing  
- `11m` — Distance on the XZ plane  
- `❤ 20.0` — Current health  
- `(123, 64, -456)` — World coordinates (X, Y, Z)

## Behavior

- Multiple players are listed vertically
- Does not modify chat, packets, or server data

## Multiplayer Safety

- Client-side only
- No commands registered
- No packets sent beyond vanilla behavior
- No gameplay advantage beyond visual awareness

## Supported Versions

- Minecraft 1.21.11
- Fabric Loader 0.18+
- Fabric API
- Java 21

## License

MIT
