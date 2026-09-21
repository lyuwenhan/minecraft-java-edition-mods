# 语言

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/shared-player-data/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/shared-player-data/README/README_ZH-CN.html)

# Shared Player Data

一个服务端户端 Fabric 模组，允许管理员通过分组让指定玩家共享同一份持久化玩家数据。

## 命令概览

- `/playerbind list`
- `/playerbind find <name>`
- `/playerbind group add <name> [name] ...`
- `/playerbind group <group> add <name> [name] ...`

说明：

- `<group>` 表示一个已经存在的组
- `<name> [name] ...` 表示一个或多个玩家名

## 指令

### 查看所有组

显示当前所有组及其成员。

```text
/playerbind list
```

### 查找玩家

查找查找玩家是否属于某个组。

```text
/playerbind find <name>
```

### 创建组

创建一个新组，并加入一个或多个在线玩家。

```text
/playerbind group add <name> [name] ...
```

示例：

```text
/playerbind group add Steve Alex
```

被添加的玩家必须：

- 当前在线
- 尚未属于其他组
- 不能在同一条指令中重复出现

### 查看组成员

显示指定组中的所有成员。

```text
/playerbind group <group> list
```

示例：

```text
/playerbind group 2 list
```

### 添加玩家

向现有组中添加一个或多个在线玩家。

```text
/playerbind group <group> add <name> [name] ...
```

示例：

```text
/playerbind group 2 add Steve Alex
```

被添加的玩家必须：

- 当前在线
- 尚未属于其他组
- 不能在同一条指令中重复出现

### 移除玩家

从现有组中移除一个或多个玩家。

```text
/playerbind group <group> remove <name> [name] ...
```

示例：

```text
/playerbind group 2 remove Steve Alex
```

只能移除当前属于该组的玩家。

玩家被移出组后：

- 不再与该组共享数据
- 该玩家的共享玩家数据会被重置
- 该玩家的管理员权限会被移除

### 删除整个组

删除一个现有组。

```text
/playerbind group <group> purge confirm
```

示例：

```text
/playerbind group 2 purge confirm
```

删除整个组时只会解除组内玩家之间的绑定关系。

不会重置原组成员的玩家数据，也不会修改他们的管理员权限。

## 共享玩家数据

同一组中的玩家会共享持久化游戏数据，包括：

- 背包
- 位置
- 经验
- 生命值
- 统计数据
- 进度
- 其他保存的玩家数据

玩家各自的 Minecraft 账号和登录身份不会改变。

## 多人游戏行为

同一组中通常只能有一名玩家同时使用共享数据。

如果另一名组内玩家在共享数据已经被使用时尝试加入服务器，会收到 Minecraft 原版的重复登录断开提示。

组外玩家不会受到影响。

## 管理员权限同步

同组已知成员之间会同步管理员权限。

如果其中一名成员获得管理员权限，其他已知成员也会同步获得管理员权限。

如果管理员权限被移除，其他已知成员的管理员权限也会同步移除。

## 注意事项

- 所有 `/playerbind` 指令都需要服务器所有者级权限 (Level 4 操作员权限)
- 玩家加入组时必须在线
- 每个玩家只能属于一个组
- 同一条指令中不能重复指定同一名玩家
- 移除玩家和删除整个组的效果不同
- 删除整个组不会重置原组成员的数据

## 支持版本

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25

## License

MIT
