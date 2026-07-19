# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/better-step/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/better-step/README/README_ZH-CN.html)

# Better Step (更好的踏步)

一个 Fabric 服务端 / 单人游戏模组，用于改进玩家在不平整地形上的踏步移动体验。

## 功能

- 让玩家在移动时更顺畅地向上踏步
- 增加受控的向下踏步行为
- 在生存类模式中避免不安全的自动向下踏步
- 创造模式下始终视为安全
- 玩家处于创造飞行时不会触发空中向上踏步
- 支持独立服务器和单人游戏

## 行为

模组会改进玩家经过高低差地形时的移动处理，增加自定义的向上踏步和向下踏步逻辑。

向上踏步行为：

- 玩家即使未完全处于着地状态，也可以尝试向上踏步
- 有助于减少小台阶、边缘或不平整地形造成的移动阻挡
- 玩家正在飞行时不会触发
- 仅作用于玩家移动

向下踏步行为：

- 如果玩家向前移动且前方没有被阻挡，模组会检查 step height 范围内是否存在可触达的较低地面
- 如果存在有效的较低地面，玩家会平滑地向下踏步
- 如果下方没有可触达地面，模组不会强制玩家向下移动
- 在非创造模式下，向下踏步高度会受到玩家安全掉落距离限制
- 在创造模式下，向下踏步始终视为安全

安全规则：

- 生存类玩家不会自动向下踏步到超过安全掉落距离的位置
- 创造模式玩家视为安全，最大向下踏步距离由 step height 决定
- 正在飞行的玩家不会触发空中向上踏步

## 支持版本

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## 许可证

MIT
