# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/shared-player-data/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/shared-player-data/README/README_ZH-CN.html)

# Shared Player Data

A server-side Fabric mod that lets selected players share the same persistent player data through administrator-managed groups.

## Command Overview

- `/playerbind list`
- `/playerbind find <name>`
- `/playerbind group add <name> [name] ...`
- `/playerbind group <group> add <name> [name] ...`

Notes:

- `<group>` means an existing group number
- `<name> [name] ...` means one or more player names

## Commands

### List Groups

Lists all existing groups and their members.

```text
/playerbind list
```

### Find Player

Shows whether a player belongs to a group.

```text
/playerbind find <name>
```

### Create Group

Creates a new group and adds one or more online players.

```text
/playerbind group add <name> [name] ...
```

Example:

```text
/playerbind group add Steve Alex
```

Players being added must:

- Be online
- Not already belong to another group
- Not appear more than once in the same command

### List Group Members

Lists all members of an existing group.

```text
/playerbind group <group> list
```

Example:

```text
/playerbind group 2 list
```

### Add Players

Adds one or more online players to an existing group.

```text
/playerbind group <group> add <name> [name] ...
```

Example:

```text
/playerbind group 2 add Steve Alex
```

Players being added must:

- Be online
- Not already belong to another group
- Not appear more than once in the same command

### Remove Players

Removes one or more players from an existing group.

```text
/playerbind group <group> remove <name> [name] ...
```

Example:

```text
/playerbind group 2 remove Steve Alex
```

Only players currently belonging to the selected group can be removed.

When a player is removed from a group:

- The player stops sharing data with the group
- The player's shared player data is reset
- The player's operator status is removed

### Purge Group

Deletes an existing group.

```text
/playerbind group <group> purge confirm
```

Example:

```text
/playerbind group 2 purge confirm
```

Purging a group only removes the binding relationship.

It does not reset the former members' player data or operator status.

## Shared Player Data

Players in the same group share persistent gameplay data, including:

- Inventory
- Position
- Experience
- Health
- Statistics
- Advancements
- Other saved player data

Minecraft accounts and authentication identities remain separate.

## Multiplayer Behavior

Only one normal player from the same group may use the shared profile at the same time.

If another member attempts to join while the shared profile is already in use, they are disconnected with Minecraft's vanilla duplicate-login message.

Players outside the group are unaffected.

## Operator Synchronization

Operator status is synchronized between known members of the same group.

If one member is granted operator status, the other known members are also granted operator status.

If operator status is removed, it is also removed from the other known members.

## Notes

- All `/playerbind` commands require owner-level server permission (Level 4 OP)
- Players must be online when being added to a group
- A player can only belong to one group
- The same player cannot be specified more than once in the same command
- Removing a player and purging a group have different effects
- Purging a group does not reset former members' data

## Supported Versions

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25

## License

MIT
