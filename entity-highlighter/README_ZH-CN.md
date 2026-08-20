# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/entity-highlighter/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/entity-highlighter/README/README_ZH-CN.html)

# Entity Highlighter (实体高亮器)

一个轻量级的 **客户端 Fabric 模组**，通过可配置的发光轮廓高亮指定实体。

## 概述

实体高亮器可让指定实体更容易被发现。

实体可以通过两种方式选择：

- 按精确实体类型 ID
- 按预设分组

每条规则都可以使用独立的六位 RGB 轮廓颜色。

## 命令

使用以下客户端命令管理规则：

```text
/entityhighlighter add type <entityid> [color]
/entityhighlighter add group <groupname> [color]
/entityhighlighter remove type <entityid> [color]
/entityhighlighter remove group <groupname> [color]
/entityhighlighter status
/entityhighlighter purge confirm
```

命令行为：

- `/entityhighlighter add type <entityid> [color]` 添加或更新精确实体类型规则
- `/entityhighlighter add group <groupname> [color]` 添加或更新预设分组规则
- `/entityhighlighter remove type <entityid> [color]` 删除对应的实体类型规则
- `/entityhighlighter remove group <groupname> [color]` 删除对应的分组规则
- `/entityhighlighter status` 列出当前所有 `type` 规则和 `group` 规则
- `/entityhighlighter purge confirm` 删除全部规则

添加规则时 `color` 参数可选。省略时默认颜色为白色

颜色必须严格使用六位十六进制 `RRGGBB` 格式。

有效示例：

```text
FF0000
00FF7F
abcdef
```

## 预设分组

可用的预设分组：

- `animal` — `MobCategory` 属于 `CREATURE`、`AMBIENT`、`AXOLOTLS`、`UNDERGROUND_WATER_CREATURE`、`WATER_AMBIENT` 或 `WATER_CREATURE` 的实体
- `monster` — `MobCategory` 属于 `MONSTER` 的实体
- `everything` — 所有实体

示例：

```text
/entityhighlighter add group animal FF0000
```

该规则会以红色轮廓高亮符合条件的动物。

## 状态

可以使用以下命令查看当前规则：

```text
/entityhighlighter status
```

输出示例：

```text
Entity Highlighter status: 1 type rule(s), 2 group rule(s)
Type rules:
  minecraft:pig 00FF00
Group rules:
  animal FF0000
  monster FFFFFF
```

## 支持版本

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
