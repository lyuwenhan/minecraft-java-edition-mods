# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/map-locator/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/map-locator/README/README_ZH-CN.html)

# Map Locator

A client-side Fabric mod that provides additional map location information, including map center coordinates, map marker coordinates, and locator bar integration while holding a map.

## Features

- Displays the map center coordinates in the map item tooltip
- Displays all map markers with available coordinates in the map item tooltip
- Registers map markers on the vanilla locator bar while the map is held in the main hand or offhand

## Coordinate Format

Map center:

```text
Map Center: X ? Z
```

Marker:

```text
Marker [Marker Name]: X ? Z
```

Example:

```text
Map Center: 1856 ? -832
Marker [Woodland Mansion]: 1923 ? -761
```

## Locator Bar

When a map containing markers with absolute coordinates is held in the main hand or offhand, Map Locator temporarily registers those markers on the vanilla Minecraft locator bar.

- Locator points use the marker's actual X/Z world coordinates
- Markers from both main-hand and offhand maps can be displayed at the same time

## Supported Versions

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25

## License

MIT