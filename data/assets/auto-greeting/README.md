# Auto Greeting

A Fabric mod that automatically sends greeting messages when **you** or **other players** join a server

## Features

- Supports multiple messages
- Supports plain chat messages and commands
- Supports blacklist / whitelist rules for player names
- Supports placeholders
- Automatically sends messages for **yourself** after joining a server
- Automatically sends messages when **other players** join

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

Controls what the mod sends.

You can use placeholders.

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

/autogreet other [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] add <message>
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

If a message **does** start with `/`, it is executed as a command.

If a message does **not** start with `/`, it is sent as a normal chat message.

## Filtering

**other**  greetings support:

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
- Cloth config 21.11.153 (Optional)
- Modmenu 17.0.0-beta.2 (Optional)

## License

MIT
