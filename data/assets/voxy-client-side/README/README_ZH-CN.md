# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/voxy-client-side/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/voxy-client-side/README/README_ZH-CN.html)

# Voxy Client Side

一个纯客户端 Fabric 模组，按照多人服务器地址分别记录世界种子，并提供 `/voxygen` 命令。

## 命令

```text
/voxygen on
/voxygen off
/voxygen status
/voxygen toggle
/voxygen radius
/voxygen radius <radius>
/voxygen seed
/voxygen seed <seed>
```

- `on`、`off`：全局启用或禁用 Voxygen。
- `status`：显示当前启用状态以及当前世界已记录的种子。
- `toggle`：切换启用状态。
- `radius`：查看当前半径，单位为区块。
- `radius <radius>`：将半径设置为正整数，默认值为 `64` 个区块。
- `seed`：查看当前世界已记录的种子。
- `seed <seed>`：本地整合服务器 seed 无法直接取得时用于手动设置；能直接读取本地 seed 时会隐藏此参数。

在单人世界中，Voxygen 会直接读取 Minecraft 整合服务器的真实 seed 并使用，不写入配置。无法读取本地 seed 时，才通过 `/voxygen seed <seed>` 显式设置。Voxygen 不会自动执行原版 `/seed`，也不会读取它的输出。

配置保存在 `config/voxy-client-side.json`，其中包含全局 `enable` 开关、生成半径 `radius`，以及使用多人服务器地址或单人存档路径作为键的 `seeds` 映射。

## 区块生成

启用后，Voxygen 会按照切比雪夫距离由近到远遍历玩家周围的正方形区域，并在后台工作线程中生成缺失区块。生成过程复用 Minecraft 原版 `ChunkGenerator`，推进至 `ChunkStatus.FEATURES`，再复用 Voxy 的体素化、mip 和世界更新流程。

搜索时会跳过客户端已加载的真实区块和 Voxy 已有数据的区块；在转换成 Voxy 区块前以及最终写入前还会再次检查，避免生成数据覆盖真实服务器地形。

目前只对客户端能用本地数据识别的原版主世界、下界和末地启用生成。当前服务器没有已记录的 seed、维度不受支持、Voxy 尚未就绪或本地世界生成注册表不完整时，生成会暂时暂停而不会猜测。仅凭 seed 无法推断服务器自定义的世界生成设置。

## 支持版本

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25

## License

MIT
