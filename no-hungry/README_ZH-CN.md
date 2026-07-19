# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/no-hungry/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/no-hungry/README/README_ZH-CN.html)

# No Hungry (无饥饿)

一个轻量级的**服务端 Fabric 模组**，用于防止玩家的饥饿值和饱和度低于可配置的最低值。

## 概述

No Hungry 可为玩家的饥饿值和饱和度设置可配置的下限。

与旧版行为不同，该模组不再持续将饥饿值和饱和度保持为满值，也不再阻止疲劳值累积。冲刺、跳跃、攻击、游泳、挖掘和 Spear Lunge 等正常生存行为仍会消耗饱和度和饥饿值。

当饥饿值或饱和度低于已配置的最低值时，模组会将其恢复到对应下限。

该模组完全在服务端运行，玩家客户端无需安装。

## 核心特性

- 可配置最低饥饿值
- 可配置最低饱和度
- 保留原版疲劳值机制
- 饥饿值和饱和度在高于配置下限时可正常下降
- 支持在游戏运行期间启用或禁用模组

## 游戏行为

No Hungry 只控制饥饿值和饱和度的最低边界。

例如，将最低饥饿值设置为 `18` 时：

- 饥饿值可以保持在 `20`、`19` 或 `18`
- 饥饿值不会低于 `18`
- 除非设置了最低饱和度，否则饱和度会按照原版规则变化
- 疲劳值会正常累积
- 冲刺、跳跃、攻击、游泳、挖掘等行为仍会正常消耗饱和度和饥饿值
- 玩家仍可正常进食
- 模组不会影响生命值、伤害、移动速度、物品栏或物品行为

## 命令

### 设置最低饥饿值

```text
/nohungry set minHunger <0-20>
```
示例：

```text
/nohungry set minHunger 18
```

该命令会将最低饥饿值设置为 `18`，并将最低饱和度设置为 `0`。

### 设置最低饱和度

```text
/nohungry set minSaturation <0-20>
```

示例：

```text
/nohungry set minSaturation 8
```

该命令会将最低饥饿值设置为 `20`，并将最低饱和度设置为 `8`。

### 启用

```text
/nohungry on
```

启用最低饥饿值和最低饱和度限制。

### 禁用

```text
/nohungry off
```

禁用模组，但不会删除当前已配置的数值。

### 切换

```text
/nohungry toggle
```

在启用和禁用状态之间切换。

### 查看状态

```text
/nohungry status
```

启用时，命令会显示：

```text
No Hungry enabled.
minimum hunger: 18
minimum saturation: 0
```

禁用时，命令会显示：

```text
No Hungry disabled.
```

## 支持版本

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
