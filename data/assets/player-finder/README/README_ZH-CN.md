# languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/player-finder/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/player-finder/README/README_ZH-CN.html)

# Player Finder (玩家查找器)

一个客户端 Fabric HUD 模组，可在 Minecraft 中直接在屏幕上显示附近玩家的信息。

## 功能特性

- 以简洁 HUD 的形式显示附近玩家（默认位于左下角）
- 显示玩家名称、方向箭头、距离、生命值和坐标
- 方向箭头相对于你当前的视角
- 在移动或转向时实时更新
- 纯客户端，可安全用于多人服务器
- 无需服务端安装

## HUD 示例

```text
Player867 ↑ 11m ❤ 20.0 (123, 64, -456)
```

字段说明：

- `Player867` —— 目标玩家名称
- `↑` —— 相对于你当前朝向的方向
- `11m` —— XZ 平面距离
- `❤ 20.0` —— 当前生命值
- `(123, 64, -456)` —— 世界坐标（X, Y, Z）

## 行为说明

- 多个玩家会按垂直方向依次列出
- 不会修改聊天、数据包或服务器数据

## 多人游戏安全性

- 仅客户端
- 不注册任何命令
- 除原版行为外不会发送额外数据包
- 除视觉感知增强外，不提供额外游戏优势

## 支持版本

- Minecraft 1.21.11
- Fabric Loader 0.18+
- Fabric API
- Java 21

## 许可证

MIT
