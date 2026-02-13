# Auto Login

Client-side Fabric mod that automatically logs you into
authentication-based Minecraft servers (e.g. EasyAuth, AuthMe)
using a locally encrypted password.

This mod runs entirely on the client and does **not** modify
any server behavior.

---

## Features

- Automatically sends `/login <password>` after joining a server
- Password is stored **locally and encrypted** (AES + PBKDF2)
- No server-side plugin or mod required
- Client-only, safe for multiplayer servers
- Manual trigger via command for testing or fallback

---

## Commands

### Set password (required once)

```
/autologin set <password>
```

- Encrypts and saves the password locally
- Enables auto-login automatically

### Trigger auto-login immediately

```
/autologin login
```

- Executes login attempt right now
- Useful for testing or manual retry

### Enable / disable

```
/autologin on
/autologin off
/autologin toggle
```

### Clear saved password

```
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
  - PBKDF2 (key derivation)
  - AES-GCM (authenticated encryption)
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
