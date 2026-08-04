# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/server-manager/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/server-manager/README/README_ZH-CN.html)

# Server Manager (服务器管理器)

一个 Fabric 服务端模组，提供基于网页的我的世界服务器管理面板。

你可以通过该面板监控服务器状态、管理在线玩家、查看日志、执行命令、编辑游戏规则，以及在安装 Carpet 后管理 Carpet 规则。

## 功能

- 基于网页的服务器管理面板
- 登录认证
- 服务器状态统计仪表盘
- 在线玩家列表
- 玩家管理
- 查看服务器日志
- 命令执行面板
- 游戏规则管理
- 安装 Carpet 后支持 Carpet 规则管理

## 页面概览

- Dashboard (仪表盘)
- Players (玩家)
- Logs (日志)
- Command (命令)
- Gamerule (游戏规则)
- Carpet Rule (Carpet 规则)

## 命令页面

命令页面允许通过网页管理面板执行服务器命令。

如果输入内容以 `/` 开头，则会将其作为命令发送。

如果输入内容不以 `/` 开头，则会将其作为 `say` 命令发送。

## 玩家管理

玩家页面会显示当前在线玩家，并提供常用的玩家管理操作。

显示的玩家信息包括：

- Name (名称)
- Status (状态)
- Dimensi (所在维度)
- Positio (坐标)
- Health (生命值)
- Hunger (饥饿值)
- Level (经验等级)
- Gamemod (游戏模式)
- Ping (延迟)

支持的玩家操作包括：

- Heal (恢复生命值)
- Feed (恢复饥饿值)
- Clear inventory (清空物品栏)
- Kill (杀死玩家)
- Kick (踢出玩家)
- Ban (封禁玩家)
- Add to whitelist (添加至白名单)
- OP (授予管理员权限)

## 访问控制

玩家页面还包含访问控制列表。

支持的列表包括：

- 白名单
- 黑名单
- 管理员列表

支持的操作包括：

- 启用或禁用白名单
- 将玩家移出白名单
- 将玩家移出黑名单
- 移除管理员权限

## 游戏规则管理

游戏规则页面会按照分类列出服务器的游戏规则。

对于每一项游戏规则，页面可以显示：

- 名称
- 类型
- 当前值
- 默认值
- 允许的值
- 推荐值

## Carpet 规则管理

Carpet 规则页面采用与游戏规则页面相同的规则管理界面。

如果服务器已安装 Carpet，则可以通过 Web 管理面板查看和修改 Carpet 规则。

## 支持版本

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25
- Carpet (可选)

## License

MIT
