# Auto Greeting

A Fabric mod that automatically sends greeting messages in the following cases:

## Client-side installation

- When **you** join a server
- When **other players** join the server

## Server-side installation

- When a player joins the server

The mod supports both plain chat messages and commands.

## Features

### Available on both client and server

- Supports multiple messages
- Supports plain chat messages and commands
- Supports blacklist / whitelist rules for player names
- Supports placeholders

### Client side

- Automatically sends messages for **yourself** after joining a server
- Automatically sends messages when **other players** join

### Server side

- Automatically sends messages when a player joins the server

## Command Overview

The root command is:

```text
/autogreet
```

Notes:

- `[a|b]` means `a` or `b`

For example:

```text
/autogreet [other|server] ...
```

means either:

```text
/autogreet other ...
```

or:

```text
/autogreet server ...
```

Depending on where the mod is installed, the following branches are available:

- **Client side**
  - `/autogreet self ...`
  - `/autogreet other ...`
- **Server side**
  - `/autogreet server ...`

## Commands

### Status

Controls whether auto greeting is **enabled** or **disabled**.

```text
/autogreet [self|other|server] status
/autogreet [self|other|server] status enable
/autogreet [self|other|server] status disable
/autogreet [self|other|server] status toggle
```

### Message

Controls what the mod sends.

You can use placeholders.

#### Placeholders for `self` / `server`

| Placeholder | Description |
|:-:|:-:|
| `@player` | Player name |
| `@UUID` | UUID |
| `@X` | X coordinate (up to 3 decimals) |
| `@Y` | Y coordinate (up to 3 decimals) |
| `@Z` | Z coordinate (up to 3 decimals) |
| `@health` | Current health |
| `@level` | Current experience level |

#### Placeholders for `other`

| Placeholder | Description |
|:-:|:-:|
| `@player` | Player name |
| `@UUID` | Player UUID |

```text
/autogreet [self|other|server] message add <message>
/autogreet [self|other|server] message add <message> <index>
/autogreet [self|other|server] message remove
/autogreet [self|other|server] message remove <index>
/autogreet [self|other|server] message remove all
/autogreet [self|other|server] message list
```

### Blacklist / Whitelist

```text
/autogreet [other|server] [whitelist|blacklist] list
/autogreet [other|server] [whitelist|blacklist] clear confirm

/autogreet [other|server] [whitelist|blacklist] [match|except] list

/autogreet [other|server] [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] add <message>
/autogreet [other|server] [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove
/autogreet [other|server] [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove <index>
/autogreet [other|server] [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove all
/autogreet [other|server] [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] list
```

## Examples

### Client side

```text
/autogreet self message add "Hello, I'm @player at (@X, @Y, @Z)"
/autogreet self message add "HP: @health | Level: @level"

/autogreet other message add "Welcome @player!"
/autogreet other message add "Hello @player (@UUID)"
```

### Server side

```text
/autogreet server message add "Welcome @player!"
/autogreet server message add "Player @player joined at (@X, @Y, @Z)"
/autogreet server message add "HP=@health Level=@level"
```

## Message Behavior

If a message does **not** start with `/`, it is sent as a normal chat message.

```text
<name> <message>
```

If a message **does** start with `/`, it is executed as a command.

## Filtering

Both **other** and **server** greetings support:

- blacklist
- whitelist
- `match` rules
- `except` rules
- `equal`
- `contain`
- `startWith`
- `endWith`

These rules are used to decide whether a joining player's name should trigger greeting logic.

### Filtering behavior

- If a player matches the **blacklist**, they are ignored
- If a player matches the blacklist but also matches **blacklist except**, they are allowed again
- If the **whitelist** is not empty, only players matching the whitelist are allowed
- If a player matches the whitelist but also matches **whitelist except**, they are ignored

## Notes

- `index` is optional and 1-based
- `add <message> <index>` inserts before the existing item at that position
- If `index` is omitted or out of range, the message is appended
- `remove <index>` removes the specified item
- `remove` without an index removes the last item
- `remove all` clears the message list
- `clear confirm` clears the entire blacklist or whitelist rule set
- Numeric values are formatted with up to 3 decimal places, with trailing zeros removed

## Supported Versions

- Minecraft 1.21.11
- Fabric Loader 0.18+
- Fabric API
- Java 21

## License

MIT
