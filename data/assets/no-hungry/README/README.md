# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/no-hungry/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/no-hungry/README/README_ZH-CN.html)

# No Hungry

A lightweight **server-side Fabric mod** that keeps player hunger and saturation permanently full, while blocking exhaustion-based hunger consumption, including Spear Lunge exhaustion cost.

---

## Overview

No Hungry prevents players from losing hunger or saturation on a Fabric server.

The mod continuously keeps every player's food level and saturation at maximum values and blocks exhaustion from being accumulated. This means normal hunger-draining actions such as sprinting, jumping, attacking, swimming, mining, and other exhaustion-based mechanics no longer reduce hunger.

Spear Lunge exhaustion consumption is also blocked when it attempts to consume hunger through the player's exhaustion system.

The mod works automatically on the server. Players do not need to install it on the client.

---

## Key Features

* Keeps player hunger level permanently full
* Keeps player saturation permanently full
* Prevents exhaustion from accumulating
* Blocks hunger loss from sprinting, jumping, attacking, swimming, and similar actions
* Blocks Spear Lunge exhaustion-based hunger consumption
* Works automatically for all players on the server
* No client-side installation required
* No keybinds
* No commands
* No configuration required
* Lightweight server-side implementation

---

## Gameplay Behavior

* Player hunger is forced to the maximum value
* Player saturation is forced to the maximum value
* Player exhaustion is reset and prevented from increasing
* Hunger does not decrease from normal survival actions
* Saturation does not decrease from exhaustion
* Players can still eat food normally, but eating is no longer required to maintain hunger
* The mod does not change health, damage, movement speed, inventory, or item behavior
* The mod does not give invulnerability
* The effect applies to all players on the server

---

## Spear Lunge Behavior

No Hungry blocks exhaustion-based hunger consumption.

When Spear Lunge attempts to consume hunger through the player exhaustion system, that exhaustion is cancelled before it can reduce saturation or hunger.

This prevents Spear Lunge from draining hunger while keeping the rest of the mechanic unchanged.

---

## Multiplayer Safety

* Server-side only
* Clients do not need to install the mod
* No custom packets required
* No client keybinds
* No client HUD changes
* No player-side configuration
* Compatible with vanilla clients
* Compatible with dedicated Fabric servers
* Safe for multiplayer servers where hunger loss should be disabled globally

---

## Supported Versions

* Minecraft 26.1.2
* Fabric Loader 0.19.2+
* Fabric API 0.150.0+
* Java 25

---

## License

MIT
