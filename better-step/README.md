# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/better-step/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/better-step/README/README_ZH-CN.html)

# Better Step

A Fabric server-side / single-player mod that improves player stepping behavior when moving across uneven terrain.

## Features

* Allows smoother stepping up while moving
* Adds controlled step-down behavior for small height drops
* Prevents unsafe automatic step-down in survival-like modes
* Treats creative mode as always safe for step-down
* Does not trigger air step-up while the player is creative-flying
* Works on dedicated servers and single-player worlds

## Behavior

The mod improves how players move across block-height changes by adding custom step-up and step-down handling.

Step-up behavior:

* Allows players to step up even when they are not fully grounded
* Helps reduce movement blocking when colliding with small ledges or uneven terrain
* Does not trigger while the player is flying
* Only applies to player movement

Step-down behavior:

* If the player moves forward without hitting an obstacle, the mod checks whether there is a reachable lower surface within step height
* If a valid lower surface is found, the player steps down smoothly
* If no reachable surface is found, the mod does not force the player downward
* In non-creative modes, step-down height is limited by the player's safe fall distance
* In creative mode, step-down is treated as safe

Safety rules:

* Survival-like players will not automatically step down farther than their safe fall distance allows
* Creative players are considered safe and may step down up to their step height
* Flying players do not trigger air step-up

## Supported Versions

* Minecraft 26.1.2
* Fabric Loader 0.19.2+
* Fabric API 0.150.0+
* Java 25

## License

MIT
