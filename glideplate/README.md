# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/glideplate/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/glideplate/README/README_ZH-CN.html)

# Glideplate

Glideplate is a Fabric mod that lets players combine a chestplate with an elytra and gives the resulting item proper Glideplate visuals.

Place a chestplate in the left anvil slot and an elytra in the right slot. The output keeps the chestplate as armor, adds gliding support, and carries item data that identifies it as a Glideplate.

When installed on the client, Glideplate also improves the appearance of marked chestplates: the item model shows an elytra under the chestplate, and equipped Glideplates render with elytra wings.

## Features

- Supports leather, copper, chainmail, iron, golden, diamond and netherite chestplates
- Combines only in one direction: chestplate on the left, elytra on the right
- Keeps the chestplate's durability, enchantments, trims and other item data
- Ignores the elytra's durability, damage and enchantments
- Updates marked chestplate item models with an elytra-and-chestplate appearance
- Keeps normal chestplates rendering normally
- Shows elytra wings when a marked chestplate is equipped
- Adds localized item names and tooltip text with English fallback text
- Costs no XP and does not add anvil prior-work penalty
- Works on dedicated servers

## Usage

Use an anvil:

1. Put a supported chestplate in the left slot.
2. Put an elytra in the right slot.
3. Take the Glideplate output.

The output item remains a chestplate, but it can also be used for elytra gliding.

## Compatibility

Glideplate can be installed on servers, clients, or both.

Install it on the server to enable anvil crafting and gameplay support. Players do not need the mod installed to join, but without the mod on their client, Glideplates will render like regular chestplates.

Install it on the client to see enhanced Glideplate item models and equipped elytra wings.

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
