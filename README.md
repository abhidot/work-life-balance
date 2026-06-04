# Boundary

Android app that **rejects incoming calls** from your blocklist when they are outside configured allowed hours — before the call connects. Built from [docs/PRD-work-life-contact-blocking.md](docs/PRD-work-life-contact-blocking.md).

## Features

- Call Screening: block matched blocklist callers off-hours (unreachable-style drop, not a manual decline)
- Global work schedule (days + time window)
- Per-contact layered overrides (days and/or times)
- Blocklist via contacts picker or manual number
- Per-entry enable/disable and master blocking pause
- Calm Material 3 UI with custom logo
- Block events stored locally (no history UI in v1)

## Requirements

- Android 10+ (API 29)
- Device must grant **Call Screening** role to Boundary

## Development

```bash
./gradlew test
./gradlew assembleDebug
```

Release APK (CI): `app/build/outputs/apk/release/app-release.apk` — minified, ~smaller than debug.

Local: `./gradlew assembleRelease`

## CI

GitHub Actions workflow [`.github/workflows/android.yml`](.github/workflows/android.yml) runs on push/PR to `main` or `master`:

1. Unit tests (`./gradlew test`)
2. Debug assemble (`./gradlew assembleDebug`)
3. Uploads `boundary-debug-apk` artifact

## Architecture

| Module | Role |
|--------|------|
| `ScheduleEvaluator` | Allowed window from global + per-entry overrides |
| `CallMatchResolver` | Match incoming E.164 to blocklist |
| `BlockingPolicy` | Master switch, enabled flag, schedule |
| `CallDecisionEngine` | Composes screening decision |
| `BoundaryCallScreeningService` | Android telecom adapter |
| `BoundaryRepository` | Room persistence |

Unit tests live under `app/src/test/java/`.
