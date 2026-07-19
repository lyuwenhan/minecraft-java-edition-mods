# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/fly-speed-modifier/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/fly-speed-modifier/README/README_ZH-CN.html)

# Fly Speed Modifier

A client-side Fabric mod that temporarily adjusts Freecam and player flight speed while you hold a key and scroll the mouse wheel.

## Features

- Supports Freecam speed multiplier adjustment
- Supports normal and direct player flight speed multiplier adjustment
- Hold the adjustment key and scroll the mouse wheel to adjust the multiplier
- Supports a configurable minimum, maximum, default multiplier, and scroll step
- Supports optionally resetting the multiplier when adjustment starts
- Full Range mode raises the available maximum from `20x` to `100x`
- The Default Speed range updates immediately when Minimum Speed or Maximum Speed changes
- If Default Speed falls outside the current minimum and maximum range, it is moved to the nearest valid boundary
- Uses non-linear mouse-wheel multiplier adjustment
- Supports Vanilla Creative Flight and [Freecam](https://modrinth.com/mod/freecam)
- Uses Left Alt as the default adjustment key

## Configuration

### Full Range

- Disabled by default
- Disabled: maximum available speed is `20x`
- Enabled: maximum available speed is `100x`
- The Maximum Speed and Default Speed sliders update immediately when this option changes

### Minimum Speed

Sets the lowest allowed temporary speed multiplier.

### Maximum Speed

Sets the highest allowed temporary speed multiplier within the range selected by Full Range.

### Default Speed

Sets the multiplier used for the first adjustment and when Reset Before Adjustment is enabled.

Its available range is always limited to the current Minimum Speed and Maximum Speed values.

### Reset Before Adjustment

- Enabled: pressing the adjustment key resets the multiplier to Default Speed
- Disabled: pressing the adjustment key keeps the previous multiplier

### Multiplier Step

Controls how quickly the multiplier changes for each mouse-wheel unit.

## Supported Versions

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25
- Mod Menu (Optional)

## License

MIT
