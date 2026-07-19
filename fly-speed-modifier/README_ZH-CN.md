# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/fly-speed-modifier/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/fly-speed-modifier/README/README_ZH-CN.html)

# Fly Speed Modifier (飞行速度修改器)

一个客户端 Fabric 模组，可在按住调速键并滚动鼠标滚轮时，临时调整 Freecam 和玩家飞行速度。

## 功能

- 支持 Freecam 速度倍数调整
- 支持普通 / 直接玩家飞行速度倍数调整
- 按住调速键并滚动鼠标滚轮即可调整倍数
- 支持配置最小值、最大值、默认值和滚轮步长
- 支持选择每次开始调整时是否重置倍数
- 关闭全范围取值时，最大速度为 `20 倍`；开启时，最大速度为 `100 倍`
- 修改最小值或最大值时，默认值的可选范围会立即更新
- 如果默认值超出当前最小值和最大值范围，会自动移动到最近的有效边界
- 鼠标滚轮使用非线性方式调整倍数
- 支持原版创造模式飞行和 [Freecam](https://modrinth.com/mod/freecam)
- 默认调速键为左 Alt

## 配置

### 全范围取值

- 默认关闭
- 关闭：最大可选速度为 `20 倍`
- 开启：最大可选速度为 `100 倍`
- 修改此选项时，最大值和默认值滑块会立即更新范围

### 速度最小值

设置速度倍数的最低值。

### 速度最大值

设置速度倍数的最高值；其可选上限由“全范围取值”决定。

### 速度默认值

设置首次调整时使用的倍数，以及启用“调整前重置”后每次开始调整时使用的倍数。

默认值的可选范围始终限制在当前最小值和最大值之间。

### 调整前重置

- 启用：按下调速键时先重置为默认值
- 禁用：按下调速键时保留上一次倍数

### 滚轮倍数步长

控制每一个鼠标滚轮单位对应的倍数调整速度。

## 支持版本

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25
- Mod Menu（可选）

## License

MIT
