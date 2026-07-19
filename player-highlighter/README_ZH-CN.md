# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/player-highlighter/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/player-highlighter/README/README_ZH-CN.html)

# Player Highlighter (玩家高亮器)

一个轻量级的**客户端 Fabric 模组**，通过发光轮廓、屏幕目标图标和附近玩家信息 HUD，帮助玩家更方便地发现和追踪其他玩家。

## 概述

玩家高亮器无需任何服务端安装，即可增强其他玩家的可见性。

本模组提供三种视觉功能：

- 为其他玩家显示发光轮廓
- 在屏幕上为视野中的玩家显示目标图标
- 在 HUD 中显示附近玩家的信息

发光轮廓和目标图标会在**按住高亮按键时（默认：TAB）**，或**启用保持模式时**显示。

玩家信息 HUD 会独立显示，不受高亮按键状态影响。

所有功能均完全由客户端处理。

## 核心功能

- 为其他玩家显示发光轮廓
- 在当前屏幕范围内为可见玩家显示目标图标
- 在 HUD 中显示附近玩家信息
- 显示玩家名称、相对方向、距离、生命值和坐标
- 支持按住按键进行临时高亮
- 支持切换保持模式进行持续高亮
- 支持通过客户端命令显示或隐藏玩家信息 HUD

## 高亮控制

满足以下任意条件时，高亮功能会被激活：

- 按住高亮按键 
  默认按键：**TAB**
- 启用保持模式 
  默认切换按键：**I**

高亮激活时：

- 其他玩家会显示发光轮廓
- 屏幕范围内的可见玩家会显示小型目标图标

当既没有按住高亮按键，也没有启用保持模式时，发光轮廓和目标图标会立即消失。

## 玩家信息 HUD

HUD 默认显示在屏幕左下角，用于列出附近玩家的信息。

示例：

```text
Player867 ↑ 11m ❤ 20.0 (123, 64, -456)
```

字段说明：

- `Player867` — 玩家名称
- `↑` — 相对于当前视角方向的方位
- `11m` — XZ 水平面上的距离
- `❤ 20.0` — 当前生命值
- `(123, 64, -456)` — 世界坐标

多个玩家会纵向排列显示。

方向指示可能使用以下箭头：

```text
↑ ↗ → ↘ ↓ ↙ ← ↖
```

当 Minecraft GUI 被隐藏时，玩家信息 HUD 也会隐藏。

也可以使用客户端命令控制 HUD 显示状态：

```text
/playerhighlighter hud on
/playerhighlighter hud off
/playerhighlighter hud toggle
/playerhighlighter hud status
```

命令行为：

- `/playerhighlighter hud on` 显示玩家信息 HUD
- `/playerhighlighter hud off` 隐藏玩家信息 HUD
- `/playerhighlighter hud toggle` 在显示和隐藏之间切换
- `/playerhighlighter hud status` 显示当前 HUD 状态

```

## 目标图标

高亮功能激活时，屏幕会在每个可见玩家的位置显示一个小型目标图标。

只有满足以下条件的玩家才会显示图标：

- 位于摄像机前方
- 能够投影到当前屏幕
- 投影位置处于屏幕边界内
- 不是当前摄像机所对应的实体

位于摄像机后方或屏幕范围外的玩家不会显示目标图标。

## 发光轮廓

其他玩家会以类似 Minecraft 原版发光效果的轮廓进行渲染。

具体行为：

- 仅影响玩家实体
- 不会高亮本地玩家自己
- 不影响生物或其他非玩家实体
- 轮廓遵循原版发光渲染规则
- 在适用情况下，仍可能使用队伍颜色
- 不会为目标玩家添加真实的发光状态效果

## 支持版本

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
