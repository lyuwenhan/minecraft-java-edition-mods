# 语言

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/hide-password/README) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/hide-password/README_ZH-CN)

# Hide Password

一个客户端 Fabric 模组，可在聊天输入框中隐藏敏感命令参数（例如 `/login`、`/register`）。

## 功能特性

- 使用固定长度的星号（********）遮蔽密码，不会泄露长度或结构
- **不会**修改实际发送到服务器的命令
- 纯客户端
- 可通过 **F8** 键开关启用 / 禁用

## 支持的命令

- `/login`、`/l`
- `/register`、`/reg`
- `/changepassword`
- `/autologin set`
- `/account unregister`
- `/account changepassword`

## 支持版本

- Minecraft 1.21.11
- Fabric Loader 0.18+
- Fabric API
- Java 21

## 许可证

MIT
