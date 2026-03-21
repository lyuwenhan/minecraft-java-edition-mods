# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/auto-login/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/data/assets/auto-login/README/README_ZH-CN.html)

# Auto Login

Client-side Fabric mod that automatically logs you into
authentication-based Minecraft servers (e.g. EasyAuth, AuthMe)
using a locally encrypted password.

This mod runs entirely on the client and does **not** modify
any server behavior.

---

## Features

- Automatically sends `/login <password>` after joining a server
- Password is stored **locally and encrypted** (AES-GCM with random device key)
- No server-side plugin or mod required
- Client-only, safe for multiplayer servers
- Manual trigger via command for testing or fallback

---

## Commands

### Set password (required once)

```text
/autologin set <password>
```

- Encrypts and saves the password locally
- Enables auto-login automatically

### Trigger auto-login immediately

```text
/autologin login
```

- Executes login attempt right now
- Useful for testing or manual retry

### Enable / disable

```text
/autologin on
/autologin off
```

### Clear saved password

```text
/autologin clear
```

- Deletes stored credentials
- Disables auto-login

---

## How It Works

1. On first setup, the password is encrypted and saved locally
2. When you join a server:
   - The mod waits until the client is fully initialized
   - Then sends the login command directly to the server
3. The password is **never sent anywhere else** and is not logged

The mod does **not** intercept packets, modify UI, or hook into
server authentication logic.

---

## Security Notes

- Passwords are stored **only on your local machine**
- Encryption uses:
  - **Random 256-bit device key** (generated per installation, not derivable from public info)
  - AES-GCM (authenticated encryption)
- The encryption key is stored in a separate file from the config
- No plaintext password is written to disk
- Do **not** reuse important real-world passwords

---

## Supported Versions

- Minecraft 1.21.11
- Fabric Loader
- Fabric API
- Java 21

---

## License

MIT
