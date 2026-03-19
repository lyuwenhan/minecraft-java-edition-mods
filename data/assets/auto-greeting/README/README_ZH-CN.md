# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/auto-greeting/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/auto-greeting/README/README_ZH-CN.html)

# Auto Greeting (自动问候)

一个 Fabric 模组，可在**你自己**或**其他玩家**加入服务器时自动发送问候消息。

## 功能特性

- 支持多条消息
- 支持普通聊天消息和命令
- 支持玩家名称黑名单 / 白名单规则
- 支持占位符
- 在**你自己**加入服务器后自动发送消息
- 在**其他玩家**加入时自动发送消息

## 命令概览

- `/autogreet self ...`
- `/autogreet other ...`

说明：

- `[a|b]` 表示 `a` 或 `b`

例如：

```text
/autogreet [other|self] status
```

表示以下两者之一：

```text
/autogreet other status
```

或：

```text
/autogreet self status
```

## 命令

### Status (状态)

控制自动问候是**启用**还是**禁用**。

```text
/autogreet [self|other] status
/autogreet [self|other] status enable
/autogreet [self|other] status disable
/autogreet [self|other] status toggle
```

### Message (消息)

控制该模组发送的内容。

你可以使用占位符。

```text
/autogreet [self|other] message add <message>
/autogreet [self|other] message add <message> <index>
/autogreet [self|other] message remove
/autogreet [self|other] message remove <index>
/autogreet [self|other] message remove all
/autogreet [self|other] message list
```

#### `self` (自己的) 可用占位符

| 占位符 | 说明 |
|:-:|:-:|
| `@player` | 玩家名称 |
| `@UUID` | UUID |
| `@X` | X 坐标（最多保留 3 位小数） |
| `@Y` | Y 坐标（最多保留 3 位小数） |
| `@Z` | Z 坐标（最多保留 3 位小数） |
| `@health` | 当前生命值 |
| `@level` | 当前经验等级 |

#### `other` (他人的) 可用占位符

| 占位符 | 说明 |
|:-:|:-:|
| `@player` | 玩家名称 |
| `@UUID` | 玩家 UUID |

### whitelist / blacklist (黑名单 / 白名单)

```text
/autogreet other [whitelist|blacklist] list
/autogreet other [whitelist|blacklist] clear confirm

/autogreet other [whitelist|blacklist] [match|except] list

/autogreet other [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] add <pattern>
/autogreet other [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove
/autogreet other [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove <index>
/autogreet other [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] remove all
/autogreet other [whitelist|blacklist] [match|except] [equal|contain|startWith|endWith] list
```

## 示例

```text
/autogreet self message add Hello, I'm @player at (@X, @Y, @Z)
/autogreet self message add HP: @health | Level: @level

/autogreet other message add Welcome @player!
/autogreet other message add Hello @player (@UUID)
```

## 消息行为

如果一条消息**以** `/` 开头，则会作为命令执行。

如果一条消息**不以** `/` 开头，则会作为普通聊天消息发送。

## 过滤规则

**other** 问候支持：

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
- Cloth config 21.11.153（可选）
- Modmenu 17.0.0-beta.2（可选）

## 许可证

MIT
