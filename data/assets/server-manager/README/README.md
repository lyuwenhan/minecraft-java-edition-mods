# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/server-manager/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/server-manager/README/README_ZH-CN.html)

# Server Manager

A Fabric server-side mod that provides a web-based Minecraft server management panel.

It allows you to monitor server status, manage online players, view logs, execute commands, edit game rules, and manage Carpet rules when Carpet is installed.

## Features

- Web-based server management panel
- Login authentication
- Dashboard for server statistics
- Online player list
- Player operations
- Server log viewer
- Command execution panel
- Gamerule management
- Carpet rule management when installed

## Page Overview

- Dashboard
- Players
- Logs
- Command
- Gamerule
- Carpet Rule

## Command Page

The Command page allows server commands to be executed from the web panel.

If the input starts with `/`, it is sent as a command.

If the input does not start with `/`, it is sent as a `say` command.

## Player Management

The Players page shows online players and provides common operations.

Displayed player information includes:

- Name
- Status
- Dimension
- Position
- Health
- Hunger
- Level
- Gamemode
- Ping

Supported player operations include:

- Heal
- Feed
- Clear inventory
- Kill
- Kick
- Ban
- Add to whitelist
- OP

## Access Control

The Players page also includes access control tables.

Supported lists:

- Whitelist
- Blacklist
- Operators

Supported operations include:

- Enable or disable whitelist
- Remove whitelist entries
- Remove blacklist entries
- Remove operator permissions

## Gamerule Management

The Gamerule page lists server gamerules by category.

For each gamerule, the page can show:

- Name
- Type
- Current value
- Default value
- Allowed values
- Recommended values

## Carpet Rule Management

The Carpet Rule page follows the same rule UI format as the Gamerule page.

If Carpet is installed, rules can be viewed and changed from the web panel.

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
