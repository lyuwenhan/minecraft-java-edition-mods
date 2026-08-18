# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/forward-lock/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/forward-lock/README/README_ZH-CN.html)

# Forward Lock (前进锁)

一个客户端 Fabric 模组，可在快速双击当前设置的前进键后，持续锁定前进输入并保持疾跑。

锁定会一直持续，直到按下当前设置的前进、后退或下蹲键。

该模组完全运行于客户端，**不会**修改任何服务器行为。

## 功能特性

- 双击当前设置的前进键，并按住疾跑键，即可启用前进锁
- 按下当前设置的前进、后退或下蹲键即可取消锁定
- 可用于普通移动、游泳和载具控制
- 锁定期间自动保持疾跑
- 遵守原版疾跑所需的饥饿值条件
- 锁定期间撞墙不会导致疾跑停止

## 使用方法

### 启用前进锁

快速按下两次当前设置的前进键，并同时按住疾跑键。

启用后，即使松开按键，模组也会持续提供前进输入。

### 取消前进锁

按下以下任意一个当前设置的控制键：

- 前进
- 后退
- 下蹲

模组会读取 Minecraft 当前设置的按键绑定，无需使用默认按键。

## 支持版本

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25

## License

MIT
