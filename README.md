# 🏃 Corsa

**Corsa** is a social running tracker for Android. It lets you record GPS runs, track your stats, follow friends, and compete in weekly distance challenges — all backed by a real-time Supabase backend.

---

## Features

### Run Tracking
- Records your route via GPS using the Fused Location Provider (high-accuracy, 3-second intervals)
- Runs as a **foreground service** so tracking continues when the screen is off or you switch apps
- Tracks distance, elapsed time, pace (min/km), elevation gain, and ambient temperature at the start location
- Route is stored as a GeoJSON LineString and rendered on an interactive MapLibre map

### Run Feed & Social
- Home feed shows your recent runs and those of people you follow
- **Like** and **comment** on any run
- Share a run via a unique deep link (`corsa://run/<token>`) that anyone can open — even without an account
- Real-time updates via Supabase Realtime so new activity appears instantly

### Run Detail
- Full-screen map view of the route
- Stats summary: distance, duration, average pace, elevation gain, temperature + weather icon
- Likes count and threaded comments

### Stats
- Personal totals: cumulative kilometres and completed weekly challenges
- Weekly km progress bar toward the level-based goal (`level × 10 km`)

### Weekly Challenge
- Every Monday a WorkManager job fires a notification reminding you to hit that week's distance goal
- The scheduler re-enqueues itself after each fire to stay DST-accurate
- Survives device reboots via a `BOOT_COMPLETED` BroadcastReceiver
- Opt in/out from Settings using a toggle persisted in DataStore

### Social — Following
- Browse all users and follow / unfollow them
- Dedicated screen to view any user's profile and their run history
- Following list with weekly km progress shown per friend

### Authentication
- **Email/password** sign-up, login, and password change
- **Google Sign-In** via Supabase ComposeAuth (native Google flow)
- Forgot-password flow using a deep-link email (`corsa://reset-password`)
- Session state observed across the whole app via `SessionViewModel`

### Profile
- Upload a custom avatar (stored in Supabase Storage)
- Edit username
- Level system: completing weekly challenges increases your level, which raises the weekly km goal

### Settings
- Change password (email accounts only)
- Toggle weekly challenge notifications
- Log out

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Navigation | Navigation Compose |
| Architecture | MVVM (ViewModel + StateFlow) |
| DI | Koin |
| Backend | Supabase (Auth, Postgrest, Realtime, Storage) |
| Maps | MapLibre Android |
| Location | Google Play Services — Fused Location Provider |
| Background work | WorkManager |
| Local storage | DataStore Preferences |
| HTTP client | Ktor (OkHttp engine) |
| Weather API | Open-Meteo |

---

## Project Structure

```
corsa/
├── data/
│   ├── location/       # GPS provider & TrackingPoint model
│   ├── model/          # Data classes: Run, Profile, Follow
│   ├── remote/         # Weather API (Open-Meteo via Ktor)
│   └── repositories/   # Auth, Runs, Profiles, NotificationPreferences
├── service/
│   ├── RunTrackingService.kt          # Foreground service for live tracking
│   └── notification/                  # Weekly challenge WorkManager job
├── ui/
│   ├── screens/
│   │   ├── auth/           # Login, Register, Reset Password
│   │   ├── home/           # Feed + active run screen
│   │   ├── rundetail/      # Map + stats + comments
│   │   ├── stats/          # Personal statistics
│   │   ├── friends/        # Follow / unfollow users
│   │   ├── profiledetail/  # Other users' profiles
│   │   └── settings/       # App settings
│   ├── composables/        # Shared UI components (TopBar, BottomBar, …)
│   ├── permissions/        # Location & notification permission handlers
│   └── theme/              # Colors, typography, spacing, shapes
└── utils/                  # Formatters, GeoJSON parser, WeatherCondition enum
```

---

## Setup

### Prerequisites
- Android Studio Hedgehog or later
- A [Supabase](https://supabase.com) project with the following tables: `profiles`, `runs`, `follows`, `likes`, `comments`
- A Google OAuth client ID configured in your Supabase project

### Configuration

Add the following to your `local.properties` (or equivalent secrets source for `BuildConfig`):

```properties
SUPABASE_URL=https://<your-project>.supabase.co
SUPABASE_KEY=<your-anon-key>
GOOGLE_CLIENT_ID=<your-google-oauth-client-id>
```

### Deep Links

The app handles two custom URI schemes:

| URI | Purpose |
|---|---|
| `corsa://reset-password` | Password reset redirect from Supabase email |
| `corsa://run/<share_token>` | Shared run deep link |

Declare these in your `AndroidManifest.xml` under `MainActivity`.

---

## Permissions

| Permission | Reason |
|---|---|
| `ACCESS_FINE_LOCATION` | GPS tracking during a run |
| `POST_NOTIFICATIONS` | Weekly challenge reminders |
| `RECEIVE_BOOT_COMPLETED` | Re-schedule notifications after reboot |
| `FOREGROUND_SERVICE` | Keep tracking alive in background |