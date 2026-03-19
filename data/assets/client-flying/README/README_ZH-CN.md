# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/client-flying/README) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/client-flying/README_ZH-CN)

# Client Flying (客户端飞行)

一个轻量级的**客户端 Fabric 模组**，可在生存模式和冒险模式下启用受控飞行行为，同时避免与鞘翅产生冲突，并且完全运行于客户端。

---

## 概述

Client Flying 允许玩家在未穿戴鞘翅时，于生存模式和冒险模式下切换飞行能力。

该模组还会强制客户端持续向服务器报告“已着地”的移动状态，并启用本地无敌标志。所有更改都完全在客户端侧处理。

当装备鞘翅时，飞行权限会被自动禁用，以避免干扰原版的滑翔机制。

---

## 核心特性

- 在生存模式和冒险模式下启用飞行
- 穿戴鞘翅时自动禁用飞行
- 持续同步客户端能力状态
- 强制发送已着地的移动数据包
- 无需命令或配置
- 轻量化，性能影响极小

---

## 视觉表现

- 在生存模式和冒险模式下可使用飞行
- 使用鞘翅时会自动禁用自定义飞行
- 每个 tick 都会发送带有已着地状态的移动数据包
- 没有任何可视化 UI 更改或覆盖层

---

## 多人游戏安全性

- 仅客户端
- 无需服务端修改
- 每个 tick 都会手动发送移动数据包
- 服务器权威仍然生效
- 在严格的反作弊服务器上可能会导致不同步问题

---

## 支持版本

- Minecraft 1.21.11
- Fabric Loader 0.18+
- Fabric API
- Java 21

---

## 许可证

MIT
