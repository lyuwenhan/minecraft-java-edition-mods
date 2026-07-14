# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/player-highlighter/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/player-highlighter/README/README_ZH-CN.html)

# Player Highlighter

A lightweight **client-side Fabric mod** that improves player awareness by combining glowing outlines, on-screen target icons, and a nearby-player information HUD.

---

## Overview

Player Highlighter makes other players easier to locate and track without requiring any server-side installation.

The mod provides three visual features:

- A glowing outline around other players
- Small target icons displayed over visible players
- A HUD list showing nearby player information

Glowing outlines and target icons appear while the configured hold key is pressed or while keep mode is enabled. The player information HUD remains available independently.

All features are handled entirely on the client.

---

## Key Features

- Highlights other players with a glowing outline
- Displays target icons over players currently visible on screen
- Shows nearby player information in a HUD
- Shows player name, relative direction, distance, health, and coordinates
- Supports a hold key for temporary highlighting
- Supports toggleable keep mode for persistent highlighting
- Supports a client command for showing or hiding the player information HUD

---

## Highlight Controls

Highlighting is active when either of the following conditions is met:

- The highlight hold key is pressed  
  Default key: **TAB**
- Keep mode is enabled  
  Default toggle key: **I**

When highlighting is active:

- Other players receive a glowing outline
- Visible players receive a small target icon at their projected screen position

When neither condition is active, the glowing outlines and target icons disappear immediately.

---

## Player Information HUD

The HUD displays nearby players in the bottom-left corner of the screen.

Example:

```text
Player867 ↑ 11m ❤ 20.0 (123, 64, -456)
```

Field description:

- `Player867` — Player name
- `↑` — Direction relative to the current camera facing
- `11m` — Horizontal distance on the XZ plane
- `❤ 20.0` — Current health
- `(123, 64, -456)` — World coordinates

Multiple players are listed vertically.

The direction indicator can use the following arrows:

```text
↑ ↗ → ↘ ↓ ↙ ← ↖
```

The HUD is hidden when Minecraft's GUI is hidden.

HUD visibility can also be controlled with the client command:

```text
/playerhighlighter hud on
/playerhighlighter hud off
/playerhighlighter hud toggle
/playerhighlighter hud status
```

Command behavior:

- `/playerhighlighter hud on` shows the Player Information HUD
- `/playerhighlighter hud off` hides the Player Information HUD
- `/playerhighlighter hud toggle` switches the HUD between shown and hidden
- `/playerhighlighter hud status` shows the current HUD state

---

## Target Icons

While highlighting is active, a small target icon is rendered over each visible player.

Icons are only displayed when the player:

- Is in front of the camera
- Can be projected onto the current screen
- Is within the visible screen boundaries
- Is not the current camera entity

Players behind the camera or outside the screen are not assigned an icon.

---

## Glowing Outline

Other players are rendered using an outline similar to Minecraft's vanilla Glowing effect.

Behavior:

- Only player entities are affected
- The local player is excluded
- Mobs and other entities are not affected
- The outline follows vanilla outline rendering rules
- Team-based outline colors may still apply where supported
- No real status effect is added to the player

---

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

---

## License

MIT
