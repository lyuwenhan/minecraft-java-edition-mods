# Auto Greeting

Client-side Fabric mod that automatically sends greeting messages when you join a Minecraft server.

## Features

- Auto-send chat messages on server join
- Auto-send chat messages while others join
- Supports multiple messages
- Supports commands and plain chat
- Client-only, safe for multiplayer servers

## Commands

```
/autogreet self status
/autogreet self status enable
/autogreet self status disable
/autogreet self status toggle

/autogreet self message add <message>
/autogreet self message add <message> [index]
/autogreet self message remove
/autogreet self message remove [index]
/autogreet self message remove all
/autogreet self message list

/autogreet other status
/autogreet other status enable
/autogreet other status disable
/autogreet other status toggle

/autogreet other message add <message>
/autogreet other message add <message> [index]
/autogreet other message remove
/autogreet other message remove [index]
/autogreet other message remove all
/autogreet other message list

/autogreet other blacklist ...
/autogreet other whitelist ...
```

Fully command list are on the bottom

### Self Greeting Placeholders

When sending messages for **yourself** (`/autogreet self ...`), the following placeholders are supported:

|Placeholder|Description|
|------------|------------|
|`@player`|Your player name|
|`@UUID`|Your UUID|
|`@X`|Your X coordinate (up to 3 decimals)|
|`@Y`|Your Y coordinate (up to 3 decimals)|
|`@Z`|Your Z coordinate (up to 3 decimals)|
|`@health`|Your current health|
|`@level`|Your experience level|

#### Example

```text
/autogreet self message add "Hello, I'm @player at (@X, @Y, @Z)"
/autogreet self message add "HP: @health|Level: @level"
```

### Other Player Greeting Placeholders

When sending messages for **other players joining** (`/autogreet other ...`), the following placeholders are supported:

|Placeholder|Description|
|------------|------------|
|`@player`|Joining player's name|
|`@UUID`|Joining player's UUID|

> Note: Position, health, and level placeholders are **not available** for other players.

#### Example

```text
/autogreet other message add "Welcome @player!"
/autogreet other message add "Hello @player (@UUID)"
```

### Notes

- index is optional and 1-based
- add <message> [index]: insert as the index-th item (before existing), or append if omitted/out of range
- remove [index]: remove specified item, or last if omitted
- All numeric values are formatted with **up to 3 decimal places**, with trailing zeros removed.
- Placeholders are replaced **client-side only**.
- No data is sent to the server beyond normal chat messages.

### Examples

```
/autogreet self status
/autogreet self add Hello
/autogreet self add I'm @player.
/autogreet self list

/autogreet other status
/autogreet other add Hi @player, welcome!
/autogreet other add Good luck, @player! 1
/autogreet other list
/autogreet other blacklist match startWith add bot_
```

## Security

- No password storage
- No encryption
- No server interaction beyond normal chat

## Supported Versions

- Minecraft 1.21.11
- Fabric Loader 0.18+
- Fabric API
- Java 21

## License

MIT

## Fully command list

```
/autogreet self status
/autogreet self status enable
/autogreet self status disable
/autogreet self status toggle

/autogreet self message add <message>
/autogreet self message add <message> [index]
/autogreet self message remove
/autogreet self message remove [index]
/autogreet self message remove all
/autogreet self message list

/autogreet other status
/autogreet other status enable
/autogreet other status disable
/autogreet other status toggle

/autogreet other message add <message>
/autogreet other message add <message> [index]
/autogreet other message remove
/autogreet other message remove [index]
/autogreet other message remove all
/autogreet other message list

/autogreet other blacklist match equal add <message>
/autogreet other blacklist match equal remove
/autogreet other blacklist match equal remove [index]
/autogreet other blacklist match equal remove all
/autogreet other blacklist match equal list
/autogreet other blacklist match contain add <message>
/autogreet other blacklist match contain remove
/autogreet other blacklist match contain remove [index]
/autogreet other blacklist match contain remove all
/autogreet other blacklist match contain list
/autogreet other blacklist match startWith add <message>
/autogreet other blacklist match startWith remove
/autogreet other blacklist match startWith remove [index]
/autogreet other blacklist match startWith remove all
/autogreet other blacklist match startWith list
/autogreet other blacklist match endWith add <message>
/autogreet other blacklist match endWith remove
/autogreet other blacklist match endWith remove [index]
/autogreet other blacklist match endWith remove all
/autogreet other blacklist match endWith list
/autogreet other blacklist match list

/autogreet other blacklist except equal add <message>
/autogreet other blacklist except equal remove
/autogreet other blacklist except equal remove [index]
/autogreet other blacklist except equal remove all
/autogreet other blacklist except equal list
/autogreet other blacklist except contain add <message>
/autogreet other blacklist except contain remove
/autogreet other blacklist except contain remove [index]
/autogreet other blacklist except contain remove all
/autogreet other blacklist except contain list
/autogreet other blacklist except startWith add <message>
/autogreet other blacklist except startWith remove
/autogreet other blacklist except startWith remove [index]
/autogreet other blacklist except startWith remove all
/autogreet other blacklist except startWith list
/autogreet other blacklist except endWith add <message>
/autogreet other blacklist except endWith remove
/autogreet other blacklist except endWith remove [index]
/autogreet other blacklist except endWith remove all
/autogreet other blacklist except endWith list
/autogreet other blacklist except list

/autogreet other blacklist list
/autogreet other blacklist clear

/autogreet other whitelist match equal add <message>
/autogreet other whitelist match equal remove
/autogreet other whitelist match equal remove [index]
/autogreet other whitelist match equal remove all
/autogreet other whitelist match equal list
/autogreet other whitelist match contain add <message>
/autogreet other whitelist match contain remove
/autogreet other whitelist match contain remove [index]
/autogreet other whitelist match contain remove all
/autogreet other whitelist match contain list
/autogreet other whitelist match startWith add <message>
/autogreet other whitelist match startWith remove
/autogreet other whitelist match startWith remove [index]
/autogreet other whitelist match startWith remove all
/autogreet other whitelist match startWith list
/autogreet other whitelist match endWith add <message>
/autogreet other whitelist match endWith remove
/autogreet other whitelist match endWith remove [index]
/autogreet other whitelist match endWith remove all
/autogreet other whitelist match endWith list
/autogreet other whitelist match list

/autogreet other whitelist except equal add <message>
/autogreet other whitelist except equal remove
/autogreet other whitelist except equal remove [index]
/autogreet other whitelist except equal remove all
/autogreet other whitelist except equal list
/autogreet other whitelist except contain add <message>
/autogreet other whitelist except contain remove
/autogreet other whitelist except contain remove [index]
/autogreet other whitelist except contain remove all
/autogreet other whitelist except contain list
/autogreet other whitelist except startWith add <message>
/autogreet other whitelist except startWith remove
/autogreet other whitelist except startWith remove [index]
/autogreet other whitelist except startWith remove all
/autogreet other whitelist except startWith list
/autogreet other whitelist except endWith add <message>
/autogreet other whitelist except endWith remove
/autogreet other whitelist except endWith remove [index]
/autogreet other whitelist except endWith remove all
/autogreet other whitelist except endWith list
/autogreet other whitelist except list

/autogreet other whitelist list
/autogreet other whitelist clear
```