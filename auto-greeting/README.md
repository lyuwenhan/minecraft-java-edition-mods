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


Depending on where the mod is installed, the following branches are available:

- **Client side**
  - `/autogreet self ...`
  - `/autogreet other ...`
- **Server side**
  - `/serverautogreet ...`

Notes:

- `[a|b]` means `a` or `b`

For example:

```text
/autogreet [other|self] ...
```

means either:

```text
/autogreet other ...
```

or

```text
/autogreet self ...
```

## Client Side Commands

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

## Server Side Commands

### Status

Controls whether auto greeting is **enabled** or **disabled**.

```text
/serverautogreet status
/serverautogreet status enable
/serverautogreet status disable
/serverautogreet status toggle
```

### Message

Controls what the mod sends.

You can use placeholders.

```text
/serverautogreet message add <message>
/serverautogreet message add <message> <index>
/serverautogreet message remove
/serverautogreet message remove <index>
/serverautogreet message remove all
/serverautogreet message list
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
/serverautogreet [whitelist|blacklist] list
/serverautogreet [whitelist|blacklist] clear confirm

/serverautogreet [whitelist|blacklist] [match|except] list

/serverautogreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] add <message>
/serverautogreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove
/serverautogreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove <index>
/serverautogreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove all
/serverautogreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] list
```

## Examples

### Client side

```text
/autogreet self message add Hello, I'm @player at (@X, @Y, @Z)
/autogreet self message add HP: @health | Level: @level

/autogreet other message add Welcome @player!
/autogreet other message add Hello @player (@UUID)
```

### Server side

```text
/serverautogreet message add Welcome @player!
/serverautogreet message add Player @player joined at (@X, @Y, @Z)
/serverautogreet message add HP=@health Level=@level
```

## Message Behavior

If a message **does** start with `/`, it is executed as a command.

If a message does **not** start with `/`, it is sent as a normal chat message.

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
