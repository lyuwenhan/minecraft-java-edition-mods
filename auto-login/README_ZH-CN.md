# Languages (语言)

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/auto-login/README) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/auto-login/README_ZH-CN)

# Auto Login (自动登录)

一个客户端 Fabric 模组，可使用本地加密存储的密码，自动登录基于身份验证的 Minecraft 服务器（例如 EasyAuth、AuthMe）。

该模组完全运行于客户端，**不会**修改任何服务器行为。

---

## 功能特性

- 在加入服务器后自动发送 `/login <password>`
- 密码以**本地加密**形式存储（AES-GCM + 随机设备密钥）
- 无需任何服务端插件或模组
- 纯客户端模组，可安全用于多人服务器
- 支持通过命令手动触发，用于测试或补救

---

## 命令

### 设置密码（首次必须执行一次）

```text
/autologin set <password>
```

- 对密码进行加密并保存在本地
- 自动启用自动登录

### 立即触发自动登录

```text
/autologin login
```

- 立即执行一次登录尝试
- 适用于测试或手动重试

### 启用 / 禁用

```text
/autologin on
/autologin off
```

### 清除已保存密码

```text
/autologin clear
```

- 删除已存储的凭据
- 禁用自动登录

---

## 工作原理

1. 首次设置时，密码会被加密并保存在本地
2. 当你加入服务器时：
   - 模组会等待客户端完全初始化
   - 然后直接向服务器发送登录命令
3. 密码**不会被发送到其他任何地方**，也不会被记录到日志中

该模组**不会**拦截数据包、修改 UI，或挂钩服务器认证逻辑。

---

## 安全说明

- 密码**仅存储在你的本地设备上**
- 加密方式使用：
  - **随机 256 位设备密钥**（每次安装独立生成，无法通过公开信息推导）
  - AES-GCM（带认证的加密）
- 加密密钥与配置文件分开存储
- 不会将明文密码写入磁盘
- **不要**复用重要的现实世界密码

---

## 支持版本

- Minecraft 1.21.11
- Fabric Loader
- Fabric API
- Java 21

---

## 许可证

MIT
