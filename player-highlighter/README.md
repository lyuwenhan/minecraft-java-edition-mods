# Player Outline Highlighter

A lightweight **client-side Fabric mod** that highlights players with a glowing outline while the player list is visible, without relying on any server-side mechanics.

---

## Overview

Player Outline Highlighter improves player visibility by showing a glowing outline around players **only when the player list menu is displayed**.  
The effect is temporary, purely visual, and handled entirely on the client.

Once the player list menu is closed, all outlines disappear immediately.

---

## Key Features

- Glowing outline appears **only while the player list menu is visible**
- No permanent highlights or screen clutter
- Purely client-side and visual-only
- Does not modify real player states or effects
- No commands, no packets, no gameplay changes
- Safe for multiplayer servers
- Lightweight with negligible performance impact

---

## Visual Behavior

- Players are rendered with a glowing outline similar to the vanilla Glowing effect
- Outline visibility is strictly tied to the player list menu
- Outline color follows vanilla rules (default or team-based, if applicable)
- No impact on mobs or non-player entities

---

## Multiplayer Safety

- Client-side only
- No server interaction
- No packets intercepted or sent
- No risk of desynchronization
- Compatible with vanilla and modded servers

---

## Supported Versions

- Minecraft 1.21.x
- Fabric Loader 0.18+
- Fabric API
- Java 21

---

## License

MIT
