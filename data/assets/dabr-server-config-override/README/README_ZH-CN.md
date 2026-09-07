# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/dabr-server-config-override/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/dabr-server-config-override/README/README_ZH-CN.html)

# DABR Server Config Override (DABR 服务端配置覆盖)

一个用于 [Do a Barrel Roll](https://modrinth.com/mod/do-a-barrel-roll) 的客户端 Fabric 模组，用于覆盖服务端 `forceEnabled` 和 `allowThrusting` 设置在客户端上的强制行为，同时保持客户端实际接收到的服务端配置不变。

## 功能

- 在客户端游戏逻辑中将服务端的 `forceEnabled` 视为未启用
- 在客户端游戏逻辑中将服务端的 `allowThrusting` 限制视为允许
- 即使服务端返回 `allowThrusting=false`，DABR 的客户端推力相关选项仍可编辑
- 取消渲染 `此服务器不允许额外推力` 对应的提醒
- DABR 的 Server Settings 页面仍显示服务端真实设置值

## 多人服务器风险

本模组会覆盖多人服务器发送到客户端的部分限制。远程服务器仍然可能纠正、拒绝或标记不符合其规则或校验逻辑的移动，也可能断开客户端连接。

仅应在服务器规则明确允许相关行为时使用。本模组不会绕过服务端移动校验或反作弊系统，模组作者不对使用本模组造成的处罚负责。

## 支持版本

- Minecraft 26.1.2
- Fabric Loader 0.19.2+
- Fabric API 0.150.0+
- Java 25
- Do a Barrel Roll (强制要求)

## License

MIT
