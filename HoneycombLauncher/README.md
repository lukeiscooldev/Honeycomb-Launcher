# Honeycomb Launcher

A from-scratch Android home-screen launcher for Jelly Bean (API 16-18) that
recreates the look and feel of the Android 3.0/3.1 Honeycomb tablet launcher
(Launcher2): Holo dark theme, holo-blue accents, a top search bar + all-apps
grid button + add button, a paged workspace, page-indicator dots, and
drag-to-place / drag-to-remove icon handling with a "Remove" zone that slides
down from the top.

**Important note on sourcing:** this build environment has no network
access, so it wasn't possible to actually pull AOSP's Launcher2 source from
Google's servers. Everything here is an original implementation written to
match Honeycomb's known layout and behavior as closely as reasonably
possible, not a copy of AOSP's code. If you want to compare directly against
the real thing, the source lives at
`platform/packages/apps/Launcher2` in the AOSP history around the 3.0/3.1
tags on android.googlesource.com.

Package name is `com.lukeiscooldev.honeylauncher` — distinct from
`com.android.launcher`/`com.android.launcher2`, so it installs alongside
your normal launcher without conflicts.

## What's implemented
- HOME-capable Activity (shows up as a launcher option)
- 5-page paged workspace with fling/snap scrolling
- Holo dark theme, holo-blue accents, page-indicator dots
- All-apps grid (drawer) sorted alphabetically, holo styling
- Drag from drawer to place an icon; drag existing icons to move them
- Drag-to-remove zone that slides down and turns red on hover
- Layout persistence (SharedPreferences) so your home screen survives a relaunch
- "+" button offering Apps / Wallpaper (wallpaper hooks the real system picker)

## What's not implemented (stretch goals)
- App widgets (real AppWidgetHost integration is a substantial addition on
  its own — happy to add it in a follow-up if you want it)
- The exact per-icon "unfold" flip animation Honeycomb used when opening
  all-apps (this uses a simpler fade/slide instead)
- A SQLite-backed model like the real launcher (SharedPreferences is enough
  for one home screen's worth of icons, but won't scale gracefully to a
  huge number of items)

## Building
Matches your existing GitHub Actions pattern — no local Android Studio
needed:
1. Push this repo to GitHub.
2. The included `.github/workflows/build.yml` builds a debug APK on every
   push to `main` (or run it manually via the Actions tab).
3. Download `app-debug.apk` from the workflow run's artifacts and side-load it.

There's no committed Gradle wrapper jar (couldn't fetch one without network
access here) — the workflow installs Gradle 8.4 directly via
`gradle/actions/setup-gradle` instead of using `./gradlew`, so this isn't
needed.

## Installing as your home screen
Side-load the APK, then either:
- Press the Home button — Android will prompt "Complete action using" and
  let you pick Honeycomb Launcher (optionally "Always"), or
- Go to Settings > Apps > Default apps > Home app and pick it there.

## Changing the package name
Edit `namespace` and `applicationId` in `app/build.gradle` — both are the
only places it's declared, so a find-and-replace across those two lines
(plus, if you rename the Java package folder, updating the `package` line
at the top of each `.java` file to match) is all it takes.
