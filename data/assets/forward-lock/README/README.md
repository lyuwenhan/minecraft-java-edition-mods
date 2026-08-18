# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/forward-lock/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/forward-lock/README/README_ZH-CN.html)

# Forward Lock

A client-side Fabric mod that locks forward movement and sprinting after quickly double-tapping the configured forward key.

The lock remains active until you press the configured forward, backward, or crouch key.

This mod runs entirely on the client and does **not** modify any server behavior.

## Features

- Double-tap the configured forward key while holding the sprint key to enable forward lock
- Press the configured forward, backward, or crouch key to disable the lock
- Works with normal movement, swimming, and vehicle controls
- Automatically maintains sprinting while the lock is active
- Respects the vanilla food requirement for sprinting
- Prevents collisions with walls from stopping sprinting while locked

## Usage

### Enable forward lock

Quickly press the configured forward key twice, while holding the sprint key.

After activation, the mod continuously supplies forward input without requiring the key to remain held.

### Disable forward lock

Press any of the following configured controls:

- Forward
- Backward
- Crouch

The mod follows the configured Minecraft key bindings. The controls do not need to remain bound to their default keys.

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
