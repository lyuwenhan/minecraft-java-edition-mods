# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/boat-utils/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/boat-utils/README/README_ZH-CN.html)

# Boat Utils

A client-side Fabric mod that adds configurable boat camera and movement utilities.

**This project is completely unrelated to [OpenBoatUtils](https://modrinth.com/mod/openboatutils). If you were looking for that project, please make sure you are visiting the [correct project](https://modrinth.com/mod/openboatutils).**

## Features

- Unlocks the vanilla boat camera rotation limit
- Makes the boat immediately follow the view direction
- Changes the left and right movement keys from turning to horizontal strafing while Follow View is enabled
- Provides four configurable boat direction hotkeys
- Snaps the boat to cardinal or 45-degree directions
- Centers the boat within its current block when using the Up, Left, or Right direction hotkeys
- Clears the boat's movement and steering inertia after using a direction hotkey
- Makes the boat use blue ice friction
- Prevents underwater sinking with a fixed upward speed of 2.4 blocks per second
- Allows underwater boarding and prevents forced dismounting while Prevent Sinking is enabled
- Provides configurable boat step height
- Provides a handbrake with an optional release boost

## Configuration

The configuration screen is available through Mod Menu.

The following options are provided:

- Unlock View Rotation
- Follow View
- Enable Direction Hotkeys
- Include 45-Degree Directions
- Increase Boat Speed
- Prevent Sinking
- Enable Handbrake
- Enable Release Boost
- Boat Step Height

### Unlock View Rotation

Removes the vanilla 180-degree horizontal view restriction while riding a boat, allowing the camera to rotate freely in a full circle.

### Follow View

Makes the boat immediately face the direction controlled by the mouse.

While enabled:

- Forward and backward movement is relative to the current view direction
- Left and right movement keys strafe horizontally instead of rotating the boat

When disabled, the boat uses the vanilla steering controls.

### Direction Hotkeys

Provides four boat direction shortcuts. They work while the local player is controlling a boat and no menu or other screen is open.

The default key bindings are:

|Key|Action|
|---|---|
|Up Arrow|Snap to the nearest allowed direction|
|Down Arrow|Turn exactly 180 degrees to the rear|
|Left Arrow|Turn left to the next allowed direction|
|Right Arrow|Turn right to the next allowed direction|

The key bindings can be changed from the Minecraft Controls screen under the **Boat Direction Hotkeys** category.

After any direction hotkey is executed, the mod:

- Clears the boat's horizontal and vertical velocity
- Clears the boat's steering rotation inertia
- Clears the current handbrake charge state

The Up, Left, and Right actions also center the boat's X and Z position within its current block.

The Down action changes only the direction and does not center the boat.

#### Allowed Directions

The **Include 45-Degree Directions** option controls which directions are considered valid:

- Enabled: every multiple of 45 degrees is allowed, providing eight directions
- Disabled: only the four cardinal directions are allowed, using 90-degree intervals

The option is enabled by default.

### Increase Boat Speed

Makes the boat use the blue ice friction value at all times.

### Prevent Sinking

While the boat is underwater, the mod sets a fixed upward speed of 2.4 blocks per second.

While this option is enabled, the mod also:

- Prevents the vanilla underwater forced-dismount timer from ejecting passengers
- Allows players to board a boat while it is underwater

These riding changes are controlled by the server. They are expected to work in singleplayer, but a remote server may reject or correct them.

### Boat Step Height

Controls the maximum height that a boat can step up while moving.

The available range is from 0.0 to 10.0 blocks.

### Handbrake

When **Enable Handbrake** is enabled, hold the jump key while controlling a boat to apply the handbrake.

The handbrake gradually reduces the boat's horizontal movement while preserving its vertical movement.

#### Release Boost

When **Enable Release Boost** is also enabled, releasing the handbrake while holding the forward movement key applies a forward boost based on how long the handbrake was held.

Longer handbrake holds produce a stronger boost, up to a maximum boost speed.

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
