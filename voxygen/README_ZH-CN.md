# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/voxygen/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/voxygen/README/README_ZH-CN.html)

# Voxygen

一个纯客户端 Fabric 模组，按照多人服务器地址分别记录世界种子，并提供 `/voxygen` 命令。

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

在单人世界中，Voxygen 会直接读取 Minecraft 整合服务器的真实 seed 并使用，不写入配置。无法读取本地 seed 时，才通过 `/voxygen seed <seed>` 显式设置。Voxygen 不会自动执行原版 `/seed`，也不会读取它的输出。

配置保存在 `config/voxygen.json`，其中包含全局开关 `enable`、进度显示开关 `showProgress`、生成半径 `radius`、工作线程数 `threads`，以及使用多人服务器地址或单人存档路径作为键的 `seeds` 映射。升级后首次启动时会自动导入已有的 `voxy-client-side.json` 配置。

## 区块生成

启用后，Voxygen 会以玩家为中心，按照基于切比雪夫距离的连续顺时针方形螺旋遍历周围区域。缺失区块由后台工作线程生成，生成过程复用 Minecraft 原版 `ChunkGenerator` 并推进至 `ChunkStatus.TERRAIN`，随后复用 Voxy 的体素化、mip 和世界更新流程。

每次只生成目标 `1x1` 区块，不生成或读取相邻区块。生成内容包括生物群系和基础地形（包括含水层流体），但会主动省略树木、结构、矿石、植被和放置型湖泊。当前体素化地形使用天空光 `15`、方块光 `0`。

是否跳过区块只依据 Voxy 中实际保存的数据，原版客户端渲染距离不会影响判断。Voxygen 会在开始生成前、体素化前和最终写入前分别检查 Voxy，避免生成数据覆盖已经从服务器收到的地形。

生成范围会跟随玩家移动。中心和半径更新由独立后台线程完成，连续变化会被合并；更新时会保留仍位于新范围内的已完成任务、活动任务和重试任务，并按照新中心的螺旋顺序重新排列剩余队列。一次更新完成后，下一次更新会等待一秒；进入有效世界两秒后才会开始首次生成。

修改工作线程数不会重启生成。降低线程数时，已经开始的多余任务会自然完成；提高线程数后则可以启动更多任务。失败区块会进入重试队列。必须更换生成会话时，旧会话会停止分配新任务，并等待活动任务完成和保存后再退出。

目前只对客户端能用本地数据识别的原版主世界、下界和末地启用生成。当前服务器没有已记录的 seed、维度不受支持、Voxy 尚未就绪或本地世界生成注册表不完整时，生成会暂时暂停而不会猜测。仅凭 seed 无法推断服务器自定义的世界生成设置。

## 支持版本

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25
- Voxy (强制要求)

## License

MIT
