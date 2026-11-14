# DERYCODE POWERED — DeryOS

One Technology. Unlimited Possibilities.

DeryOS is the operating system programme of Derycode Technologies. This repository
holds the first working software of that programme: an interactive interface
prototype, a real Android launcher, and the backend services that will carry
Derycode ID, the App Market, DeryAI and Derycode Cloud.

**What this is:** a Phase 1 prototype. What it is not: a finished operating
system. No system partitions are modified, no kernel work is present, and the
interface you see is an application, not a running OS image.

## What actually works

| Part | State |
| --- | --- |
| Interface prototype (`prototype/`) | Working, clickable: home, launcher, notifications, quick settings, settings, market, DeryAI, Derycode ID, device info |
| Android launcher (`android/`) | Real HOME-intent launcher in Kotlin + Compose that lists installed apps and launches them |
| Backend API (`backend/`) | Running: registration, sign in, JWT sessions, device registry, market listing, install records, AI gateway with offline fallback |
| AOSP image | Not started, Phase 5 |

## Run the interface prototype

```bash
cd prototype
python3 -m http.server 8080
# open http://localhost:8080
```

Or open `prototype/index.html` directly in a browser. It is a single file with
no build step and no dependencies.

## Run the backend

```bash
cd backend
cp .env.example .env          # then set JWT_SECRET
npm install
npm start                     # http://localhost:8787/health
```

Endpoints:

```
GET  /health
POST /v1/auth/register   { email, password, displayName }
POST /v1/auth/login      { email, password }
GET  /v1/profile         (Bearer token)
POST /v1/devices         { deviceName, platform, build }
GET  /v1/market/apps
POST /v1/market/install  { appId }
POST /v1/ai/chat         { message }
```

## Build the Android launcher

```bash
cd android
./gradlew assembleDebug        # requires Android SDK 34 and JDK 17
adb install app/build/outputs/apk/debug/app-debug.apk
```

Press Home and Android will offer DeryOS as a home screen.


## Install the launcher on a phone

Download the built APK (15 MB, Android 8.0 and newer):

- https://derycode.online/apps/deryos/DeryOS-launcher-0.1.2.apk
- https://github.com/asiimwe3/deryos/releases/latest

Open it on the phone, allow installs from the browser if Android asks, then press
Home and choose DeryOS. To go back, clear DeryOS as the default home app.

Tap the Settings tile on the home screen to change the display preset: HD+, FHD+,
QHD+ or 4K detail. The launcher re-renders at the density you choose, which is how
an ordinary app can change resolution without system privileges.

Rebuild it yourself with JDK 17 and Android SDK 34:

```bash
cd android
echo "sdk.dir=/path/to/Android/sdk" > local.properties
gradle assembleDebug        # or ./gradlew assembleDebug with a wrapper
```

## Layout

```
deryos/
├── prototype/        single-file interface prototype (Phase 1 deliverable)
├── android/          Kotlin + Jetpack Compose launcher (Phase 2 foundation)
├── backend/          Node API: identity, market, AI gateway
│   └── sql/          Postgres schema for the same records
└── docs/             architecture, security, roadmap, install
```

## Continue in order

1. `docs/ARCHITECTURE.md` — how the parts fit and what the AOSP step changes.
2. `docs/SECURITY.md` — the controls already in code and the ones still owed.
3. `docs/ROADMAP.md` — eight phases with exit criteria.
4. `docs/INSTALL.md` — every command, including the AOSP toolchain.
