# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/seedgen/README/README.html)|[ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/seedgen/README/README_ZH-CN.html)

# Seedgen (种子生成)

一个客户端 Fabric 模组, 根据已知世界种子在客户端本地生成原版地形, 无需服务器加载或生成每一个来源区块。

支持的模组：[Voxy](https://modrinth.com/mod/voxy)、[Xaero's World Map](https://modrinth.com/mod/xaeros-world-map)、[JourneyMap](https://modrinth.com/plugin/journeymap)。

> [!IMPORTANT]
> 多人服务器生成必须使用完全正确的世界种子。错误的种子会产生与服务器不一致的地形。受 [MC-55596](https://bugs.mojang.com/browse/MC/issues/MC-55596) 影响, 即使种子正确, 树木等生成地物也有可能与服务器不一致。
>
> 种子按服务器和世界 (维度) 分别保存。当服务器运行多个种子不同的世界 (例如大厅世界、资源世界和建筑世界) 时, 请站在各个世界中分别用 `/seedgen seed <seed>` 设置各自的种子。当一个世界包含多个维度时, 请在每个维度中输入相同的种子；原版下界和末地会自动继承已为主世界设置的种子。

## 命令

|命令|说明|
|---|---|
|`/seedgen on`|启用种子生成。|
|`/seedgen off`|禁用种子生成并停止当前会话。|
|`/seedgen toggle`|切换启用状态。|
|`/seedgen status`|显示世界、种子、半径、进度、线程使用情况和结果统计。|
|`/seedgen radius`|查看半径, 单位为原版区块。|
|`/seedgen radius <radius>`|设置半径。默认 `64`, 最小 `1`。|
|`/seedgen threads`|查看工作线程数。|
|`/seedgen threads <threads>`|设置 `1` 至 `256` 个工作线程, 默认 `32`。|
|`/seedgen progress`|查看进度 BossBar 是否启用。|
|`/seedgen progress on\|off\|toggle`|控制进度 BossBar, 默认关闭。|
|`/seedgen generateforfreecam`|查看是否在 Freecam 位置生成。|
|`/seedgen generateforfreecam on\|off\|toggle`|控制是否针对当前摄像机周围区域生成, 默认关闭。|
|`/seedgen generateforworldmapcenter`|查看世界地图中心位置生成是否启用。|
|`/seedgen generateforworldmapcenter on\|off\|toggle`|将打开的 Xaero 或 JourneyMap 全屏地图中心作为生成目标, 默认关闭。|
|`/seedgen seed`|查看当前世界的有效种子。|
|`/seedgen seed <seed>`|设置当前世界的多人服务器种子；本地游戏可直接提供种子时隐藏。|

## 支持版本

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25
- Voxy (可选)
- Xaero's World Map (可选)
- JourneyMap (可选)

## License

MIT
