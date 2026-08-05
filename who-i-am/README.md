# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/who-i-am/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/who-i-am/README/README_ZH-CN.html)

# Who I Am

A client-side Fabric mod that displays your own Minecraft player name in places where it is normally hidden.

This mod runs entirely on the client and does **not** modify any server behavior.

## Features

- Shows your own name tag in third-person view
- Shows your own name tag when the camera is detached from the local player, including free-camera views
- Appends the current player name to the end of the Minecraft window title
- Provides the client-side `/i` command to display the current player name
- No server-side plugin or mod required

## Commands

### Show current player name

```text
/i
```

- Prints `You are: <player name>` in the client chat
- The command is handled locally and is not sent to the server

## How It Works

1. The mod reads the current player name from the active Minecraft client session
2. When the local player model is rendered:
   - The name tag is forced to display in third-person view
   - The name tag is also forced to display when the camera is detached from the local player
3. Whenever Minecraft generates the window title, the player name is appended to the end
4. Running `/i` displays the same player name in the client chat

The mod does **not** send additional packets, require server support, or modify server-side player data.

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
