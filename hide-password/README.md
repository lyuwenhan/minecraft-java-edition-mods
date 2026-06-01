# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/hide-password/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/hide-password/README/README_ZH-CN.html)

# Hide Password

Client-side Fabric mod that hides sensitive command parameters
(e.g. /login, /register) in the chat input.

## Features

- Masks passwords with fixed-length stars (********) — no length/structure disclosure
- **Does NOT** modify actual command sent to server
- Client-only
- Toggle on/off via **F8** key

## Supported Commands

- `/login`, `/l`
- `/register`, `/reg`
- `/changepassword`
- `/autologin set`
- `/account unregister`
- `/account changepassword`

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
