# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/shared-player-data/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/shared-player-data/README/README_ZH-CN.html)

# Shared Player Data

A **server-side Fabric mod** that allows selected online players to share the same persistent player profile data through explicit administrator binding.

## Overview

Shared Player Data lets server administrators bind players together so that they use a shared player data profile.

After players are bound, they share the same persistent gameplay state, including vanilla player data such as inventory, position, experience, health, statistics, advancements, and related saved profile data.

Only one player from the same bound group may be online at the same time. If another player from the same group attempts to join while the shared profile is already in use, the join is rejected with the vanilla duplicate-login message.

## Key Features

- Bind two online players with a simple command
- Bound players share the same persistent player profile
- Only one player from the same bound group can be online at once
- Uses the vanilla duplicate-login disconnect message when a bound profile is already in use
- Supports online-player name suggestions for commands
- Automatically saves binding configuration
- Synchronizes operator status within bound groups
- Designed for dedicated servers
- No client-side installation required

## Commands

```mcfunction
/playerbind <name1> <name2>
```

### Behavior

- Both players must be online
- `<name1>` remains online
- `<name2>` is bound to the same shared profile group and then disconnected
- The command requires level 4 permission
- The command can be used by level 4 operators, the server console, and RCON

### Restrictions

- The first and second player cannot be the same player
- A player cannot bind themselves as the second target

## Operator Synchronization

When players are in the same bound group, their operator status is synchronized.

If one member of the group is granted operator status, the other known members of the group will also receive operator status.

If one member of the group is removed from the operator list, the other known members of the group will also be removed from the operator list.

## Multiplayer Behavior

- Bound players share one persistent gameplay profile
- Only one bound player may use the shared profile at a time
- Other players in the same bound group receive the vanilla duplicate-login disconnect message while the profile is in use
- Players outside the bound group are unaffected

## Server Safety

- Server-side only
- Dedicated-server focused
- No client mod required
- Does not require players to install anything
- Does not change player authentication identity
- Keeps real player accounts separate while sharing bound gameplay data
- Prevents simultaneous access to the same shared profile

## Configuration

The mod stores its configuration in:

```text
config/shared-player-data.json
```

Bindings created through commands are saved automatically.

Manual configuration edits should be made while the server is stopped.

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
