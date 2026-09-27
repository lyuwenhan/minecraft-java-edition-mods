# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/map-locator/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/map-locator/README/README_ZH-CN.html)

# Map Locator (地图定位器)

一个客户端 Fabric 模组，可为地图提供额外的定位信息，包括地图中心点、地图标记点坐标，以及在手持地图时将标记点显示在定位栏中。

## 功能特性

- 在地图物品的物品提示中显示地图中心点坐标
- 在地图物品的物品提示中显示所有可获取坐标的标记点
- 主手或副手持有地图时，将地图标记点注册到原版定位栏

## 坐标显示格式

地图中心点：

```text
地图中心点: X ? Z
```

标记点：

```text
标记点 [标记名称]: X ? Z
```

例如：

```text
地图中心点: 1856 ? -832
标记点 [林地府邸]: 1923 ? -761
```

## 定位栏

当主手或副手持有包含绝对标记坐标的地图时，地图定位器会将这些标记点临时注册到我的世界的原版定位栏。

- 定位点使用标记自身的 X/Z 世界坐标
- 同时持有两张地图时，会同时显示两张地图中的标记点

## 支持版本

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25

## License

MIT