# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/entity-highlighter/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/entity-highlighter/README/README_ZH-CN.html)

# Entity Highlighter

A lightweight **client-side Fabric mod** that highlights selected entities with configurable glowing outlines.

## Overview

Entity Highlighter makes selected entities easier to locate without requiring any server-side installation.

Entities can be selected in two ways:

- By exact entity type ID
- By preset group

Each rule can use its own six-digit RGB outline color.

## Commands

Rules are managed with the client command:

```text
/entityhighlighter add type <entityid> [color]
/entityhighlighter add group <groupname> [color]
/entityhighlighter remove type <entityid> [color]
/entityhighlighter remove group <groupname> [color]
/entityhighlighter status
/entityhighlighter purge confirm
```

Command behavior:

- `/entityhighlighter add type <entityid> [color]` adds or updates an exact entity type rule
- `/entityhighlighter add group <groupname> [color]` adds or updates a preset group rule
- `/entityhighlighter remove type <entityid> [color]` removes the matching entity type rule
- `/entityhighlighter remove group <groupname> [color]` removes the matching group rule
- `/entityhighlighter status` lists all configured type rules and group rules
- `/entityhighlighter purge confirm` removes all configured rules

The `color` argument is optional when adding a rule. If omitted, the default color is white.

Colors must contain exactly six hexadecimal digits in `RRGGBB` form.

Valid examples:

```text
FF0000
00FF7F
abcdef
```

## Preset Groups

The following preset groups are available:

- `animal` — entities in the `CREATURE`, `AMBIENT`, `AXOLOTLS`, `UNDERGROUND_WATER_CREATURE`, `WATER_AMBIENT`, or `WATER_CREATURE` categories
- `monster` — entities in the `MONSTER` category
- `everything` — every entity

Example:

```text
/entityhighlighter add group animal FF0000
```

This highlights matching animals with a red outline.

## Status

The current rules can be displayed with:

```text
/entityhighlighter status
```

Example output:

```text
Entity Highlighter status: 1 type rule(s), 2 group rule(s)
Type rules:
  minecraft:pig 00FF00
Group rules:
  animal FF0000
  monster FFFFFF
```

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
