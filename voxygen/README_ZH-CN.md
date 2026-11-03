# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/voxygen/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/voxygen/README/README_ZH-CN.html)

# Voxygen

一个客户端 Fabric 模组，通过按服务器记录的种子和客户端命令，将未生成的原版区块生成到 Voxy 中。

## 命令

```text
/voxygen on
/voxygen off
/voxygen status
/voxygen toggle
/voxygen radius
/voxygen radius <radius>
/voxygen threads
/voxygen threads <threads>
/voxygen progress
/voxygen progress on|off|toggle
/voxygen seed
/voxygen seed <seed>
```

- `on`、`off`：全局启用或禁用 Voxygen。
- `status`：显示当前启用状态以及当前世界已记录的种子。
- `toggle`：切换启用状态。
- `radius`：查看当前半径，单位为区块。
- `radius <radius>`：将半径设置为正整数，默认值为 `64` 个区块。
- `threads`：查看当前工作线程数。
- `threads <threads>`：将工作线程数设置为 `1` 至 `256`，默认值为 `32`。
- `progress`：查看生成进度 BossBar 是否启用。
- `progress on|off|toggle`：控制黄色的 `Voxygen` 生成进度 BossBar。默认关闭，生成完成两秒后自动消失。
- `seed`：查看当前世界已记录的种子。
- `seed <seed>`：本地整合服务器 seed 无法直接取得时用于手动设置；能直接读取本地 seed 时会隐藏此参数。

在单人世界中，Voxygen 会直接读取 Minecraft 整合服务器的真实 seed 并使用，不写入配置。无法读取本地 seed 时，才通过 `/voxygen seed <seed>` 显式设置。

## 支持版本

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25
- Voxy (强制要求)

## License

MIT
