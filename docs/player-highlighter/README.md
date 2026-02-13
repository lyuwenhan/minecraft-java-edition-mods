# Player Highlighter

A lightweight **client-side Fabric mod** that highlights players with a glowing outline while a designated key is held or a toggle is enabled, without relying on any server-side mechanics.

---

## Overview

Player Highlighter improves player visibility by showing a glowing outline around other players **while the hold key is pressed (default: TAB)** or **when the highlight toggle is enabled**.  
The effect is purely visual, handled entirely on the client, and can be either momentary or persistent based on user preference.

When neither the hold key is pressed nor the toggle is enabled, all outlines disappear immediately.

---

## Key Features

- Glowing outline appears **while holding the configured key** or **when keep mode is enabled**
- Optional toggle mode for persistent highlighting
- No screen clutter when inactive
- Purely client-side and visual-only
- Does not modify real player states or effects
- No commands, no packets, no gameplay changes
- Safe for multiplayer servers
- Lightweight with negligible performance impact

---

## Visual Behavior

- Other players are rendered with a glowing outline similar to the vanilla Glowing effect
- The local player is never highlighted
- Outline visibility is controlled by:
  - Holding the highlight key (default: TAB), or
  - Enabling keep mode via a toggle key
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

- Minecraft 1.21.11
- Fabric Loader 0.18+
- Fabric API
- Java 21

---

## License

MIT
