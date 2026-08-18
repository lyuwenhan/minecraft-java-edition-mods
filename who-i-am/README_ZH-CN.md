# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/who-i-am/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/who-i-am/README/README_ZH-CN.html)

# Who I Am (我是谁)

一个客户端 Fabric 模组，用于在 Minecraft 通常隐藏自身玩家名称的位置显示自己的名称。

该模组完全运行于客户端，**不会**修改任何服务器行为。

## 功能特性

- 在第三人称视角下显示自己的名称标签
- 当相机与本地玩家分离时显示自己的名称标签，包括自由视角场景
- 在 Minecraft 窗口标题末尾追加当前玩家名称
- 提供客户端指令 `/i`，用于显示当前玩家名称
- 无需任何服务端插件或模组

## 命令

### 显示当前玩家名称

```text
/i
```

- 在客户端聊天栏中输出 `You are: <玩家名称>`
- 该指令完全由客户端处理，不会发送到服务器

## 工作原理

1. 模组从当前 Minecraft 客户端会话中读取玩家名称
2. 当本地玩家模型被渲染时：
   - 在第三人称视角下强制显示名称标签
   - 当相机与本地玩家分离时，同样强制显示名称标签
3. 每当 Minecraft 生成窗口标题时，将玩家名称追加到标题末尾
4. 执行 `/i` 时，在客户端聊天栏中显示相同的玩家名称

该模组**不会**发送额外数据包、要求服务端支持，或修改服务端玩家数据。

## 支持版本

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
