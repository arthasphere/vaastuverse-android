# Android app + api-gateway on this Windows PC

Use this when you run **Docker** on Windows and the **VaastuVerse Android** app on a **phone or emulator** on the same LAN.

## 1. Same network

- PC and phone on the **same Wi‑Fi** (or phone on Wi‑Fi that can reach the PC’s subnet).
- **Android emulator on the same PC as Docker:** you usually do **not** need this doc for reachability — the app uses **`http://10.0.2.2:8080`**, which maps to the host from the emulator.

## 2. Find your Windows IPv4

```bat
ipconfig
```

Under your **Wi‑Fi** adapter, copy **IPv4 Address** (example: `192.168.0.47`).

## 3. Confirm the gateway is listening

On the **PC**:

```bat
curl http://127.0.0.1:8080/actuator/health
```

From **`vaastuverse-local`**: `docker compose ps` and check **api-gateway** publishes **8080:8080** (or your mapped port).

## 4. Windows Firewall (physical device)

Allow **inbound TCP 8080** for the profile you use (Private recommended):

- **Windows Security** → **Firewall & network protection** → **Advanced settings** → **Inbound Rules** → **New Rule…** → Port → TCP → **8080** → Allow.

Or (elevated PowerShell):

```powershell
New-NetFirewallRule -DisplayName "VaastuVerse api-gateway 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```

## 5. Point the Android build at your PC

In the **project root** `local.properties` (same folder as `settings.gradle.kts`), set:

```properties
vaastuverse.gatewayHost=192.168.0.47
```

Use **your** IPv4. **Sync Gradle** in Android Studio so `BuildConfig.GATEWAY_HOST` updates.

The app treats **emulator vs physical device** in `ApiConfig.kt`: emulators use **10.0.2.2**; real devices use **`BuildConfig.GATEWAY_HOST`**.

## 6. Smoke test from the phone

In **Chrome** on the device:

`http://<YOUR-PC-IP>:8080/actuator/health`

You should see JSON with `"status":"UP"` (or similar). If this fails, fix network/firewall before debugging the app.

## 7. In-app ping

**Root** screen → **Backend** card → **Ping gateway** calls `…/actuator/health` using the same base URL logic as the rest of the app.

## Troubleshooting

| Symptom | Likely cause |
|--------|----------------|
| Emulator OK, phone fails | Wrong `vaastuverse.gatewayHost`, different VLAN/guest Wi‑Fi, or firewall |
| Browser on phone fails | Same as above; fix before the app |
| `Connection refused` on emulator | Docker not running or gateway not bound to `0.0.0.0:8080` |
| SSL errors | You are on **HTTP** in dev; production should use **HTTPS** and a proper cert |
