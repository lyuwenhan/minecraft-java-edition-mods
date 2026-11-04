# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/voxygen/README/README.html)|[ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/voxygen/README/README_ZH-CN.html)

# Voxygen

一个客户端 Fabric 模组，在客户端本地生成原版地形，再将结果提交给 [Voxy](https://modrinth.com/mod/voxy)，从而填充远处的 Voxy 地形，而不要求服务器加载或生成每一个来源区块。

> [!IMPORTANT]
> 多人服务器生成必须使用完全正确的世界种子。错误的种子会产生与服务器不一致的地形。受 [MC-55596](https://bugs.mojang.com/browse/MC/issues/MC-55596) 影响，即使种子正确，树木等生成地物也有可能与服务器不一致。

## 命令

|命令|说明|
|---|---|
|`/voxygen on`|启用 Voxygen。|
|`/voxygen off`|禁用 Voxygen 并停止当前会话。|
|`/voxygen toggle`|切换启用状态。|
|`/voxygen status`|显示世界、种子、半径、进度、线程使用情况和结果统计。|
|`/voxygen radius`|查看半径，单位为原版区块。|
|`/voxygen radius <radius>`|设置半径。默认 `64`，最小 `1`。|
|`/voxygen threads`|查看工作线程数。|
|`/voxygen threads <threads>`|设置 `1` 至 `256` 个工作线程，默认 `32`。|
|`/voxygen progress`|查看进度 BossBar 是否启用。|
|`/voxygen progress on\|off\|toggle`|控制进度 BossBar，默认关闭。|
|`/voxygen seed`|查看当前世界的有效种子。|
|`/voxygen seed <seed>`|设置多人服务器种子；整合服务器可直接提供种子时隐藏。|

## 性能说明

- 区块生成会大量使用 CPU 和内存。建议逐步提高 `threads`，不要直接使用全部逻辑处理器。
- 半径增大时，大区块总数会按平方增长。

## 支持版本

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25
- Voxy (强制要求)

## License

MIT
