# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/infinity-fireworks/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/infinity-fireworks/README/README_ZH-CN.html)

# Infinity Fireworks (无限烟花)

一个适用于 Minecraft 的 Fabric 模组，可让烟花火箭在使用后不消耗物品数量。

## 功能特性

- 对方块使用烟花火箭时不会消耗数量
- 使用烟花火箭进行鞘翅加速时不会消耗数量
- 在单人游戏中通过集成服务器生效
- 安装在 Fabric 服务端时可在多人服务器生效
- 保留原版烟花行为，包括飞行时间数据和爆炸效果
- 无需命令或配置

## 使用示例

```text
使用烟花火箭 -> 烟花正常发射 -> 物品数量保持不变
```

字段说明：

- `使用烟花火箭` - 右键方块，或在鞘翅飞行时加速
- `烟花正常发射` - 原版烟花行为仍然发生
- `物品数量保持不变` - 物品堆叠数量不会减少

## 行为说明

- 适用于所有烟花火箭，包括自定义烟花
- 不会添加新物品，也不会修改合成配方
- 不影响发射器行为
- 创造模式行为保持不变

## 多人游戏安全性

- 仅在客户端安装时，不会改变远程服务器上的物品消耗
- 多人服务器需要在服务端安装本模组才会生效
- 安装在服务器后，服务器中的玩家使用烟花都会触发不消耗行为
- 本模组不注册命令，也不发送自定义玩法数据包

## 支持版本

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
