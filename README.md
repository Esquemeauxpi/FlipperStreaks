# Flipper Streaks

A personal pinball play tracker built on the [Pinball Map API](https://pinballmap.com/api/v1/docs).
Streaks, badges, and history are fully local and work with no account.
Connecting a Pinball Map account is optional and only needed to edit the
real shared map (add machines, leave comments, submit high scores).

## Opening the project

1. Open this folder in Android Studio (Koala or newer recommended).
2. Let Gradle sync — it will pull dependencies from Google's Maven and
   Maven Central (this environment couldn't reach those repos to verify
   a build, so double-check version numbers still resolve when you sync).
3. Run on a device/emulator with API 26+.

## Architecture

See the module tree under `app/src/main/java/com/flipperstreaks/app`:

- `data/local` — Room (play logs, cached locations/machines) +
  EncryptedSharedPreferences (optional Pinball Map auth token)
- `data/remote` — Retrofit interface for `pinballmap.com/api/v1`, plus an
  `AuthInterceptor` that attaches credentials only when a user has connected
- `data/repository` — `PlayLogRepository` (pure local, no network dependency
  at all), `LocationRepository` (cached reads), `AuthRepository` (optional
  connect/disconnect lifecycle)
- `domain` — framework-free models, use cases, and `StreakEngine` (pure
  streak/badge calculation logic — easy to unit test in isolation)
- `ui` — one package per screen, each with a `@HiltViewModel` +
  `@Composable`, wired together in `ui/navigation/FlipperStreaksNavHost.kt`

## What's stubbed vs. real

**Real / wired end-to-end:**
- Room schema, DAOs, database
- Retrofit API surface matching Pinball Map's actual endpoints
- Full local play-logging flow (Nearby → Location detail → log a play →
  Streaks screen updates automatically via Flow)
- `StreakEngine` badge logic (day streaks, decade diversity, manufacturer
  diversity, location diversity)
- Optional login flow: landing sheet → form → encrypted token storage →
  account screen reflects connection state

**Explicitly stubbed — marked with `// TODO` in source:**
- Real device location (`NearbyScreen` currently hardcodes a Portland,
  OR fallback lat/lon — swap in `FusedLocationProviderClient`)
- Map screen (currently a placeholder — see the earlier chat mockup for
  target visual: played vs. unvisited location dots)
- Streak heatmap visual (currently a text placeholder in `BadgeCard.kt`
  — swap for a real 7×N grid)
- "Played today" detection in `MachineRow` (always false right now —
  needs a query against today's `PlayLogDao` entries per machine)
- "Create a Pinball Map account" currently a no-op — should open
  `pinballmap.com/users/signup` via Custom Tabs

## A note on API usage

Pinball Map is a small, donation-funded, open-source project. Their docs
ask that integrators avoid bursty request patterns and reach out before
scaling up. This scaffold caches reads through Room with a TTL and keeps
all write traffic behind the optional login gate — see
`LocationRepository` and `AuthInterceptor` for where that's enforced.
