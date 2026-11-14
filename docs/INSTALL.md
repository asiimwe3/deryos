# Install and test

## 1. Interface prototype

Requirements: any modern browser. Nothing else.

```bash
cd prototype
python3 -m http.server 8080
```

Open `http://localhost:8080`. On a phone, use your computer's LAN address.

What to exercise:

1. Boot screen, then tap to skip.
2. Lock screen: click or swipe up to unlock.
3. Home screen: tap tiles; unavailable apps explain their phase.
4. Status bar: opens quick settings. Notifications tab is beside it.
5. Market: install an app and watch progress and installed state persist.
6. DeryAI: tap a suggestion or type a question.
7. Settings: change wallpaper, toggle battery saver and reduce motion.
8. Gesture bar at the bottom returns home from anywhere.

## 2. Backend

Requirements: Node 20+.

```bash
cd backend
cp .env.example .env
node -e "console.log(require('crypto').randomBytes(48).toString('hex'))"   # JWT_SECRET
npm install
npm start
```

Verify:

```bash
curl localhost:8787/health
curl -X POST localhost:8787/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"you@example.com","password":"at-least-10-chars"}'
```

The register response includes a token. Use it as `Authorization: Bearer <token>`
against `/v1/profile`, `/v1/market/install` and `/v1/ai/chat`.

## 3. Android launcher

Requirements: JDK 17, Android SDK 34, a device with USB debugging or an emulator.

```bash
cd android
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Then press Home and pick DeryOS. To go back to the stock launcher, clear DeryOS
as the default home app in Settings, Apps, Default apps.

## 4. AOSP build (Phase 5, not yet started)

This is recorded so the path is unambiguous, not because it can run today.

```bash
repo init -u https://android.googlesource.com/platform/manifest -b android-14.0.0_r1
repo sync -j8
source build/envsetup.sh
lunch aosp_arm64-userdebug
m
```

Then fork `packages/apps/Launcher3`, drop in the DeryOS launcher, replace
`bootanimation.zip`, and add vendor configuration. A working build requires
matching vendor blobs for the target device, which is the real work of Phase 5.

## Troubleshooting

| Symptom | Cause | Fix |
| --- | --- | --- |
| Blank page in prototype | Javascript blocked by an extension | Open in a normal window |
| API exits immediately in production | `JWT_SECRET` unset | Set it in `.env` |
| `./gradlew` permission denied | Archive lost the bit | `chmod +x gradlew` |
| Launcher not offered as home | Another app is default | Clear defaults for the current launcher |
