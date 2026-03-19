# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/auto-greeting-server/README) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/auto-greeting-server/README_ZH-CN)

# Auto Greeting - Server

A Fabric mod that automatically sends greeting messages when a player joins the server

The mod supports both plain chat messages and commands.

## Features

- Supports multiple messages
- Supports plain chat messages and commands
- Supports blacklist / whitelist rules for player names
- Supports placeholders
- Automatically sends messages when a player joins the server

## Command Overview

  - `/servergreet ...`

Notes:

- `[a|b]` means `a` or `b`

For example:

```text
/servergreet [whitelist|blacklist] list
```

means either:

```text
/servergreet whitelist list
```

or

```text
/servergreet blacklist list
```

## Server Side Commands

### Status

Controls whether auto greeting is **enabled** or **disabled**.

```text
/servergreet status
/servergreet status enable
/servergreet status disable
/servergreet status toggle
```

### Message

Controls what the mod sends.

You can use placeholders.

```text
/servergreet message add <message>
/servergreet message add <message> <index>
/servergreet message remove
/servergreet message remove <index>
/servergreet message remove all
/servergreet message list
```

#### Placeholders

| Placeholder | Description |
|:-:|:-:|
| `@player` | Player name |
| `@UUID` | UUID |
| `@X` | X coordinate (up to 3 decimals) |
| `@Y` | Y coordinate (up to 3 decimals) |
| `@Z` | Z coordinate (up to 3 decimals) |
| `@health` | Current health |
| `@level` | Current experience level |

### Blacklist / Whitelist

```text
/servergreet [whitelist|blacklist] list
/servergreet [whitelist|blacklist] clear confirm

/servergreet [whitelist|blacklist] [match|except] list

/servergreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] add <pattern>
/servergreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove
/servergreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove <index>
/servergreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove all
/servergreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] list
```

## Examples

```text
/servergreet message add Welcome @player!
/servergreet message add Player @player joined at (@X, @Y, @Z)
/servergreet message add HP=@health Level=@level
```

## Message Behavior

If a message **does** start with `/`, it is executed as a command.

If a message does **not** start with `/`, it is sent as a normal chat message.

## Filtering

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
