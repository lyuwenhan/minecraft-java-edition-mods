# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/auto-login/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/auto-login/README/README_ZH-CN.html)

# Auto Greeting - Server (服务端自动问候)

一个 Fabric 模组，可在玩家加入服务器时自动发送问候消息。

该模组同时支持普通聊天消息和命令。

## 功能特性

- 支持多条消息
- 支持普通聊天消息和命令
- 支持玩家名称黑名单 / 白名单规则
- 支持占位符
- 在玩家加入服务器时自动发送消息

## 命令概览

- `/servergreet ...`

说明：

- `[a|b]` 表示 `a` 或 `b`

例如：

```text
/servergreet [whitelist|blacklist] list
```

表示以下两者之一：

```text
/servergreet whitelist list
```

或：

```text
/servergreet blacklist list
```

## 服务端命令

### Status (状态)

控制自动问候是**启用**还是**禁用**。

```text
/servergreet status
/servergreet status enable
/servergreet status disable
/servergreet status toggle
```

### Message (消息)

控制该模组发送的内容。

你可以使用占位符。

```text
/servergreet message add <message>
/servergreet message add <message> <index>
/servergreet message remove
/servergreet message remove <index>
/servergreet message remove all
/servergreet message list
```

#### 占位符

| 占位符 | 说明 |
|:-:|:-:|
| `@player` | 玩家名称 |
| `@UUID` | UUID |
| `@X` | X 坐标（最多保留 3 位小数） |
| `@Y` | Y 坐标（最多保留 3 位小数） |
| `@Z` | Z 坐标（最多保留 3 位小数） |
| `@health` | 当前生命值 |
| `@level` | 当前经验等级 |

### Blacklist / Whitelist (黑名单 / 白名单)

```text
/servergreet [whitelist|blacklist] list
/servergreet [whitelist|blacklist] clear confirm

/servergreet [whitelist|blacklist] [match|except] list

/servergreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] add <pattern>
/servergreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove
/servergreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove <index>
/servergreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove all
/servergreet [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] list
```

## 示例

```text
/servergreet message add Welcome @player!
/servergreet message add Player @player joined at (@X, @Y, @Z)
/servergreet message add HP=@health Level=@level
```

## 消息行为

如果一条消息**以** `/` 开头，则会作为命令执行。

如果一条消息**不以** `/` 开头，则会作为普通聊天消息发送。

## 过滤规则

支持以下内容：

- blacklist
- whitelist
- `match` 规则
- `except` 规则
- `equal`
- `contain`
- `startWith`
- `endWith`

这些规则用于判断某个加入玩家的名称是否应触发问候逻辑。

### 过滤行为

- 如果玩家匹配**黑名单**，则会被忽略
- 如果玩家匹配黑名单，但同时匹配**blacklist except**，则会重新被允许
- 如果**白名单**不为空，则只允许匹配白名单的玩家
- 如果玩家匹配白名单，但同时匹配**whitelist except**，则会被忽略

## 说明

- `index` 为可选参数，且从 1 开始计数
- `add <message> <index>` 会插入到该位置现有项目之前
- 如果省略 `index` 或其超出范围，则消息会追加到末尾
- `remove <index>` 会删除指定项目
- 不带索引的 `remove` 会删除最后一项
- `remove all` 会清空消息列表
- `clear confirm` 会清空整个黑名单或白名单规则集
- 数值最多保留 3 位小数，并去除末尾多余的 0

## 支持版本

- Minecraft 1.21.11
- Fabric Loader 0.18+
- Fabric API
- Java 21

## 许可证

MIT
