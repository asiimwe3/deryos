# DeryOS security architecture

## Principle

The client is untrusted. Anything that matters — identity, payments, model keys,
market entitlements — lives behind the API and is decided on the server.

## Controls present in this build

| Area | Control | Where |
| --- | --- | --- |
| Passwords | bcrypt, cost 12 | `backend/src/server.js` |
| Sessions | JWT, 12h expiry, signed with server secret | `backend/src/server.js` |
| Key handling | Model keys read from env only; no key in any client file | `backend/.env.example` |
| Secret hygiene | `.env`, keystores and `node_modules` git-ignored | `.gitignore` |
| Brute force | 10 requests/min on credential endpoints, 20/min on AI | `express-rate-limit` |
| Account enumeration | Identical `invalid_credentials` for unknown email and bad password | `POST /v1/auth/login` |
| Input limits | 32 kB JSON body cap, 4 000 char message cap | `express.json`, `/v1/ai/chat` |
| Transport | HTTPS only in deployment; HSTS at the edge | host config |
| Startup safety | Refuses to boot in production without `JWT_SECRET` | `backend/src/server.js` |

## Controls still owed before public release

1. **Refresh tokens and rotation.** A 12h access token is the whole session today.
   Add short-lived access tokens plus rotating refresh tokens bound to a device.
2. **Two-factor authentication.** The interface shows the switch; the server
   needs TOTP enrolment, backup codes and recovery.
3. **Device revocation.** `dery_devices.revoked_at` exists; enforcement must
   reject tokens issued to a revoked device.
4. **Encryption at rest.** Account records, device records and any file content
   in Derycode Cloud must be encrypted with keys in a managed KMS.
5. **Update signing.** System and market updates must be signed and verified on
   device; a compromised update channel is a total compromise.
6. **Permission model.** The launcher will need notification access, and that
   request must be explicit, reversible and explained in plain language.
7. **Audit trail.** Security-relevant events (sign-in, device add, revoke,
   permission grant) should be written to append-only storage.

## Threat model, briefly

| Threat | Mitigation path |
| --- | --- |
| Stolen phone | Device-bound tokens, remote revoke, screen lock requirement |
| Credential stuffing | Rate limits, breach-password checks, 2FA |
| Malicious app in the market | Review pipeline, signature verification, permission disclosure |
| Model key theft | Server-only key, per-account quotas, no key in the interface |
| Update hijack | Signed manifests, staged rollout, rollback capability |
| Malicious sideloaded launcher | System partition install in Phase 5, not user install |

## Non-negotiables for contributors

- No API keys, passwords, tokens or keystores in any file that ships to a client.
- No secrets in commit history. If one lands, rotate it — removing the file is
  not enough.
- No client-side decisions about entitlements or payments.
- Any new endpoint that changes state requires authentication and a rate limit.
