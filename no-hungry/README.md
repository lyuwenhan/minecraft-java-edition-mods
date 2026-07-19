# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/no-hungry/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/no-hungry/README/README_ZH-CN.html)

# No Hungry

A lightweight **server-side Fabric mod** that prevents player hunger and saturation from dropping below configurable minimum values.

---

## Overview

No Hungry adds configurable lower limits for player hunger and saturation.

Unlike the original behavior, the mod no longer keeps hunger and saturation permanently full and no longer blocks exhaustion. Normal survival mechanics such as sprinting, jumping, attacking, swimming, mining, and Spear Lunge can still consume saturation and hunger.

When hunger or saturation drops below the configured minimum, the mod restores it to that minimum value.

The mod works entirely on the server. Players do not need to install it on the client.

---

## Key Features

- Configurable minimum hunger level
- Configurable minimum saturation level
- Normal exhaustion mechanics remain active
- Hunger and saturation can decrease normally above the configured minimum
- Supports enabling and disabling the mod at runtime

---

## Gameplay Behavior

No Hungry only controls the lower boundary of hunger and saturation.

For example, with the minimum hunger level set to `18`:

- Hunger can remain at `20`, `19`, or `18`
- Hunger cannot remain below `18`
- Saturation behaves normally unless a minimum saturation value is configured
- Exhaustion continues to accumulate normally
- Sprinting, jumping, attacking, swimming, mining, and similar actions still consume saturation and hunger
- Players can still eat food normally
- The mod does not affect health, damage, movement speed, inventory, or item behavior

---

## Commands

### Set Minimum Hunger

```text
/nohungry set minHunger <0-20>
```

Example:

```text
/nohungry set minHunger 18
```

This sets the minimum hunger level to `18` and the minimum saturation level to `0`.

### Set Minimum Saturation

```text
/nohungry set minSaturation <0-20>
```

Example:

```text
/nohungry set minSaturation 8
```

This sets the minimum hunger level to `20` and the minimum saturation level to `8`.

### Enable

```text
/nohungry on
```

Enables minimum hunger and saturation enforcement.

### Disable

```text
/nohungry off
```

Disables the mod without deleting the configured value.

### Toggle

```text
/nohungry toggle
```

Switches the mod between enabled and disabled.

### Status

```text
/nohungry status
```

When enabled, the command displays:

```text
No Hungry enabled.
minimum hunger: 18
minimum saturation: 0
```

When disabled, the command displays:

```text
No Hungry disabled.
```

---

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

---

## License

MIT
