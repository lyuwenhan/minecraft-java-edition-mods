# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/auto-greeting/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/auto-greeting/README/README_ZH-CN.html)

# Auto Greeting

A Fabric mod that automatically sends preset greeting messages when **you** or **other players** join a server.

## Features

- Supports multiple messages
- Supports plain chat messages as well as commands
- Supports blacklist / whitelist rules for player names
- Supports placeholders
- Automatically sends preset messages after **you** join a server
- Automatically sends preset messages when **other players** join a server

## Command Overview

- `/autogreet self ...`
- `/autogreet other ...`

Notes:

- `[a|b]` means `a` or `b`

For example:

```text
/autogreet [other|self] status
```

means either:

```text
/autogreet other status
```

or

```text
/autogreet self status
```

## Commands

### Status

Controls whether auto greeting is **enabled** or **disabled**.

```text
/autogreet [self|other] status
/autogreet [self|other] status enable
/autogreet [self|other] status disable
/autogreet [self|other] status toggle
```

### Message

Controls the messages sent by the mod.

Placeholders are supported.

```text
/autogreet [self|other] message add <message>
/autogreet [self|other] message add <message> <index>
/autogreet [self|other] message remove
/autogreet [self|other] message remove <index>
/autogreet [self|other] message remove all
/autogreet [self|other] message list
```

#### Placeholders for `self`

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

### Blacklist / Whitelist

```text
/autogreet other [whitelist|blacklist] list
/autogreet other [whitelist|blacklist] clear confirm

/autogreet other [whitelist|blacklist] [match|except] list

/autogreet other [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] add <pattern>
/autogreet other [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove
/autogreet other [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove <index>
/autogreet other [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove all
/autogreet other [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] list
```

## Examples

```text
/autogreet self message add Hello, I'm @player at (@X, @Y, @Z)
/autogreet self message add HP: @health | Level: @level

/autogreet other message add Welcome @player!
/autogreet other message add Hello @player (@UUID)
```

## Message Behavior

If a message starts with `/`, it is executed as a command.

If a message does not start with `/`, it is sent as a normal chat message.

## Filtering

**other** greetings support:

- blacklist
- whitelist
- `match` rules
- `except` rules
- `equal`
- `contain`
- `startWith`
- `endWith`

These rules determine whether the name of a joining player should trigger greeting logic.

### Filtering behavior

- If a player matches the **blacklist**, they are ignored
- If a player matches the blacklist but also matches **blacklist except**, they are allowed again
- If the **whitelist** is not empty, only players matching the whitelist are allowed
- If a player matches the whitelist but also matches **whitelist except**, they are ignored

## Notes

- `index` is optional and 1-based
- `add <message> <index>` inserts before the existing item at that position
- If `index` is omitted or outside the valid range, the message is appended
- `remove <index>` removes the specified item
- `remove` without an index removes the last item
- `remove all` clears the message list
- `clear confirm` clears the entire blacklist or whitelist rule set
- Numeric values are formatted with up to 3 decimal places, and trailing zeros are removed

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25
- Cloth config (Optional)
- Modmenu (Optional)

## License

MIT
