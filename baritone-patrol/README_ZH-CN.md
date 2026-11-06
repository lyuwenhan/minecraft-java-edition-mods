# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/baritone-patrol/README/README.html)|[ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/baritone-patrol/README/README_ZH-CN.html)

# Baritone Patrol (Baritone 自动巡逻)

一个客户端 Fabric 模组，它可以按顺序记录一条路线，并控制 Baritone 在这些路径点之间持续循环巡逻。

## 命令

|命令|说明|
|---|---|
|`/patrol add`|将玩家当前所在的方块坐标添加到路线末尾。|
|`/patrol add <x> <y> <z>`|将指定的整数坐标添加到路线末尾。|
|`/patrol start`|从第一个路径点开始巡逻。|
|`/patrol stop`|停止本模组正在执行的巡逻。没有巡逻时不会取消其他 Baritone 目标。|
|`/patrol clear`|删除当前世界或服务器保存的全部路径点。|
|`/patrol list`|按顺序显示已经记录的路径点。|

## 支持版本

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25
- [Baritone](https://github.com/cabaletta/baritone) (Required)

## License

MIT
