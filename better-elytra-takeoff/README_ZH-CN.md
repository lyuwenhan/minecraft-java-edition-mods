# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/better-elytra-takeoff/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/better-elytra-takeoff/README/README_ZH-CN.html)

# Better Elytra Takeoff (鞘翅助飞)

一个适用于 Fabric 的服务端 / 单人游戏模组，可让玩家在地面使用烟花火箭直接启动鞘翅滑翔。

## 功能

- 在地面使用烟花火箭启动鞘翅
- 起飞后立即获得烟花助推
- 支持专用服务器和单人游戏
- 无需命令或配置文件

## 行为

玩家使用烟花火箭时，如果满足起飞条件，模组会取消原版使用事件，使玩家进入滑翔状态，并生成助推烟花。

起飞需要：

- 手持烟花火箭
- 穿戴带有滑翔能力的装备，例如鞘翅
- 没有下蹲
- 当前不在滑翔
- 不是旁观者、没有骑乘实体、没有处于创造飞行

准心规则：

- 如果准心选中方块，玩家视角需要保持水平或朝向水平以上
- 如果准心没有选中方块，则任意朝向都可以起飞

## 支持版本

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
