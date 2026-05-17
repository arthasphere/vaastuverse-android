# VaastuVerse Android (Jetpack Compose)

Kotlin / **Jetpack Compose** scaffold mirroring the **iOS** app: customer + partner shells, shared **theme** tokens, **`UserSessionViewModel`**, and **`ApiConfig`** for the **api-gateway** on your desktop (**Docker**).

## Quick test (emulator on the same Windows PC as Docker)

1. Start the stack from **`vaastuverse-local`**: `docker compose up -d` (gateway on host **port 8080**).
2. Open this folder in **Android Studio** (Hedgehog+). Let it create **`local.properties`** with `sdk.dir=…` if missing.
3. Run the **app** configuration on a **Pixel / API 34** emulator.

The app uses **`http://10.0.2.2:8080`** on emulators (Android’s alias for the host machine). No LAN IP needed for that case.

Optional: open **Chrome on the emulator** to `http://10.0.2.2:8080/actuator/health` to confirm routing.

## Physical phone + Docker on this Windows PC

See **[`CONNECT_ANDROID_WINDOWS.md`](CONNECT_ANDROID_WINDOWS.md)** (firewall, `ipconfig`, `local.properties`).

Summary:

1. Same Wi‑Fi as the PC.
2. Add to **`local.properties`** (project root, next to `settings.gradle.kts`):

   ```properties
   vaastuverse.gatewayHost=192.168.x.x
   ```

   Use your PC’s **IPv4** from `ipconfig` (Wireless adapter). Re-sync Gradle after changing it.

3. Windows **Firewall**: inbound **TCP 8080** (same as iOS).
4. **Device browser** smoke test: `http://<PC-IP>:8080/actuator/health`.

## Project layout

| Area | Notes |
|------|--------|
| `data/ApiConfig.kt` | Builds gateway base URL (emulator **10.0.2.2** vs device **BuildConfig.GATEWAY_HOST**). |
| `data/UserSessionViewModel.kt` | Same demo names / copy helpers as iOS `UserSession`. |
| `ui/RootShellScreen.kt` | Dev **Customer / Partner** switch (like iOS `RootShellView`). |
| `ui/customer/` | Bottom nav: Home, Reports, Muhurats, Ask expert. |
| `ui/partner/` | Guruji / Designer / Channel tab shells (simplified vs iOS but same structure). |
| `design-reference/` | HTML mocks (copied from iOS repo for side‑by‑side work). |

## Production

Use **HTTPS**, a real hostname, tighten **`network_security_config`**, and remove cleartext dev flags.
