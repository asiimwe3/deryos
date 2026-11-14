# DeryOS roadmap

Each phase has an exit criterion. A phase is done when the criterion is met, not
when the work feels finished.

## Phase 1 — Brand and interface prototype  ✅ complete
Delivered: interaction prototype, brand system (deep navy, electric blue, white,
silver), boot animation concept, screen inventory, architecture and security
documents.

**Exit criterion:** a person can be handed the prototype and understand the
product without narration. Met.

## Phase 2 — Functional Android launcher  🔄 in progress
Scope: ship the Kotlin launcher as the user's home screen. Real app grid, app
search, wallpaper selection, per-app shortcuts, widget support, settings
persistence, notification listener for the notification centre.

**Exit criterion:** the developer's own phone runs DeryOS as its daily launcher
for one week without falling back to the stock launcher.

## Phase 3 — Derycode App Market
Scope: authenticated catalogue, versioned package delivery, install and update
flow, developer upload with signing, entitlement checks server-side.

**Exit criterion:** one third-party developer publishes an app that installs on
another person's DeryOS device without manual help.

## Phase 4 — DeryAI integration
Scope: server-side model gateway, on-device intents for system actions, context
from notifications and calendar with explicit consent, quota enforcement.

**Exit criterion:** DeryAI answers a system question and performs one system
action correctly from natural language, with the permission prompt shown.

## Phase 5 — AOSP customisation
Scope: AOSP sync, Launcher3 fork, privileged Derycode services, boot animation,
fonts, sounds, defaults, `vendor/derycode` configuration, first flashed build.

**Exit criterion:** a device boots a signed DeryOS image where the Derycode
launcher is the only home screen and OTA updates install.

## Phase 6 — Device compatibility and security testing
Scope: support matrix across reference devices, battery and performance budgets,
penetration test of identity and market, fuzzing of package handling, rollback of
a bad update in the field.

**Exit criterion:** signed-off security review with no open critical findings,
and one successful rollback drill.

## Phase 7 — First public demonstration
Scope: public site, live demo phones and desktop preview, developer programme
opening, founding cohort of market developers.

**Exit criterion:** a public demo where an outsider completes sign-up, installs
an app and uses DeryAI unaided.

## Phase 8 — Independent operating system exploration
Scope: evaluate replacing AOSP components with in-house services, evaluate
non-Android kernels for tablet and desktop lines, decide the desktop stack
(Linux base with Derycode shell).

**Exit criterion:** a technical decision document with a build-vs-adopt analysis
and a funded path.

## Desktop and tablet lines

DeryOS Desktop starts from a Linux base with the Derycode shell layered above it.
DeryOS Tablet reuses the Android platform with a tablet-optimised launcher. Both
inherit Derycode ID, the market and DeryAI, so no product is stranded.
