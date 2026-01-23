# Auto Greeting

Client-side Fabric mod that automatically sends greeting messages when you join a Minecraft server.

## Features

- Auto-send chat messages on server join
- Supports multiple messages
- Supports commands and plain chat
- Client-only, safe for multiplayer servers

## Commands

```
/auto-greeting self on
/auto-greeting self off
/auto-greeting self status
/auto-greeting self add <message> [index]
/auto-greeting self remove [index]
/auto-greeting self removeAll
/auto-greeting self list

/auto-greeting other on
/auto-greeting other off
/auto-greeting other status
/auto-greeting other add <message> [index]
/auto-greeting other remove [index]
/auto-greeting other removeAll
/auto-greeting other list
```

Notes:
- index is optional and 1-based
- add <message> [index]: insert as the index-th item (before existing), or append if omitted/out of range
- remove [index]: remove specified item, or last if omitted

### Examples

```
/auto-greeting self status
/auto-greeting self add Hello
/auto-greeting self add I'm @player.
/auto-greeting self list

/auto-greeting other status
/auto-greeting other add Hi @player, welcome!
/auto-greeting other add Good luck, @player! 1
/auto-greeting other list
```

`@player` will be replaced with the target player’s username automatically.

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
