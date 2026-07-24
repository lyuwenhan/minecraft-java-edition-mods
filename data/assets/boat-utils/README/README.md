# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/boat-utils/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/boat-utils/README/README_ZH-CN.html)

# Boat Utils

A client-side Fabric mod that adds configurable boat camera and movement utilities.

## Features

- Unlocks the vanilla boat camera rotation limit
- Makes the boat immediately follow the view direction
- Changes the left and right movement keys from turning to horizontal strafing while Follow View is enabled
- Makes the boat use blue ice friction
- Prevents underwater sinking with a fixed upward speed of 2.4 blocks per second
- Allows underwater boarding and prevents forced dismounting while Prevent Sinking is enabled
- Provides configurable boat step height

### Unlock View Rotation

Removes the vanilla 180-degree horizontal view restriction while riding a boat, allowing the camera to rotate freely in a full circle.

### Follow View

Makes the boat immediately face the direction controlled by the mouse.

While enabled:

- Forward and backward move relative to the current view direction
- Left and right strafe horizontally instead of rotating the boat

When disabled, the boat uses the vanilla steering controls.

### Increase Boat Speed

Makes the boat use the blue ice friction value

### Prevent Sinking

While the boat is underwater, its gives a vertical speed

While this option is enabled, the mod also:

- Prevents the vanilla underwater forced dismount timer from ejecting passengers
- Allows players to board a boat while it is underwater

These riding changes are controlled by the server. They are expected to work in singleplayer, but a remote server may reject or correct them.

### Boat Step Height

Controls the maximum height the boat can step up from 0 to 10 while moving.

## Multiplayer Warning

This mod changes client-controlled boat movement. A server may correct, reject, or flag the resulting movement.

Use the mod only where server rules permit it. The mod author is not responsible for warnings, kicks, bans, or other penalties caused by its use.

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25
- Mod Menu (Optional)

## License

MIT
