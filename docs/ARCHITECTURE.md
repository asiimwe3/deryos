# DeryOS architecture

## The shape of the programme

DeryOS is built in layers so that each one can ship without waiting for the next.

```
┌──────────────────────────────────────────────────────────────┐
│  Surface layer      home, launcher, notifications, settings  │  Phase 1–2
├──────────────────────────────────────────────────────────────┤
│  Service layer      Derycode ID, App Market, DeryAI, Cloud   │  Phase 3–4
├──────────────────────────────────────────────────────────────┤
│  Platform layer     AOSP framework and system services       │  Phase 5
├──────────────────────────────────────────────────────────────┤
│  Kernel layer       Linux kernel, drivers, HAL                │  Phase 5–6
└──────────────────────────────────────────────────────────────┘
```

Everything above the platform layer is ordinary application code. That is
deliberate: it means the product can be demonstrated, sold and iterated while
the harder system work is still ahead.

## Component map

### 1. Interface prototype — `prototype/index.html`

A single self-contained file. It exists to settle the interaction model before
any of it is rewritten in Kotlin. Five modules, each isolated so it can be
ported one to one:

| Module | Responsibility |
| --- | --- |
| `store` | Persistent user state: theme, toggles, Derycode ID, installed apps |
| `registry` | Catalogue of apps and their destinations |
| `router` | Screen stack with back behaviour |
| `surfaces` | Status bar, notification centre, quick settings panel |
| `apps` | Market, DeryAI, settings, profile, device screens |

State is written to `localStorage` under a versioned key (`deryos.state.v1`) so
a future build can migrate rather than discard it.

### 2. Android launcher — `android/`

The launcher is the first piece of real system software. It declares
`CATEGORY_HOME`, which makes Android treat it as a home-screen candidate, and it
queries `PackageManager` for launchable activities on that device.

- `MainActivity` — single entry point, edge-to-edge, hosts the Compose tree.
- `BootScreen` — brand and boot animation concept, then hands off to home.
- `launcher/HomeScreen` — real installed-app grid; tapping launches the app.
- `ui/theme` — the DERYCODE POWERED palette in one place.

Phase 2 replaces the gesture and notification surfaces with real system
integrations (`NotificationListenerService`, `AccessibilityService` for the
recents gesture). Phase 5 does not rewrite this app: it moves it into
`packages/apps/` inside the AOSP tree and gives it system privileges.

### 3. Backend — `backend/`

One Node service today, split by domain tomorrow.

| Domain | Endpoints | Notes |
| --- | --- | --- |
| Derycode ID | `/v1/auth/*`, `/v1/profile` | bcrypt cost 12, JWT 12h, opaque failure messages |
| Devices | `/v1/devices` | Account-scoped registry, revocable in the schema |
| App Market | `/v1/market/*` | Published-only listing, install records |
| DeryAI | `/v1/ai/chat` | Server-side model proxy with rate limit and offline fallback |

Storage is in-memory in the prototype and Postgres in the target design; the SQL
in `backend/sql/schema.sql` is the contract both sides write to. When the real
database arrives, only the storage functions change, not the routes.

### 4. Data model

```
dery_accounts 1 ──< dery_devices
      │
      └──< dery_installs >── dery_market_apps
```

- `dery_accounts` — identity; `derycode_id` is the human-facing handle.
- `dery_devices` — one row per signed-in device; `revoked_at` supports remote sign-out.
- `dery_market_apps` — catalogue, `published` gates visibility.
- `dery_installs` — the join that makes "my apps" possible per account.

## The AOSP step, honestly described

Phase 5 is where the project stops being an app and starts being a system. The
work is:

1. Sync an AOSP tag that matches the reference device's vendor blobs.
2. Fork `packages/apps/Launcher3` and replace it with this launcher.
3. Ship Derycode services as privileged system apps with their own signature.
4. Replace the boot animation (`bootanimation.zip`), system fonts, sounds, and
   default wallpapers in the product partition.
5. Add `vendor/derycode` for device configuration, then build and flash.

Nothing in that list is done yet. The repository is arranged so none of it
forces a rewrite: the launcher is already an app, the services are already HTTP,
and the branding already lives in one theme file.

## Why a web prototype first

A Compose prototype requires a device or emulator for every reviewer. A single
HTML file runs anywhere, on any phone, in seconds, and it settles questions that
are expensive to change later: information hierarchy, gesture meaning, panel
behaviour, notification density. The Kotlin launcher then implements decisions
rather than exploring them.
