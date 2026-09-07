# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/dabr-server-config-override/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/dabr-server-config-override/README/README_ZH-CN.html)

# DABR Server Config Override

A client-side Fabric mod for [Do a Barrel Roll](https://modrinth.com/mod/do-a-barrel-roll) that overrides the client-side enforcement of the server's `forceEnabled` and `allowThrusting` settings while keeping the received server configuration unchanged.

## Features

- Treats the server's `forceEnabled` setting as disabled for client gameplay behavior
- Treats the server's `allowThrusting` restriction as allowed for client gameplay behavior
- Keeps DABR's client-side thrusting options editable even when the server reports `allowThrusting=false`
- Suppresses the `Extra thrusting is not allowed on this server` notification
- Keeps DABR's Server Settings screen showing the server's real values

## Multiplayer Warning

This mod overrides client-side restrictions sent by a multiplayer server. A remote server may still correct, reject, flag, or disconnect clients for movement that violates its rules or validation logic.

Use this mod on multiplayer servers only when the server rules explicitly allow the resulting behavior. The mod does not bypass server-side movement validation or anti-cheat systems, and the mod author is not responsible for penalties caused by its use.

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25
- Do a Barrel Roll (Required)

## License

MIT
