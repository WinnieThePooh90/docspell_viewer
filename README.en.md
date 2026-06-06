# Docspell Viewer (Android)

**Languages:** [Deutsch](README.md) · **English**

Android app (**Kotlin + Jetpack Compose**) for reading and searching documents on your **own Docspell server** via the REST API v1.

## Requirements

| | |
|--|--|
| **Android** | 8.0 (API 26) or higher |
| **Docspell server** | Reachable URL `https://<host>/api/v1/` |
| **Sign-in** | **Local account login** (`collective/user` + password) must be enabled on the server |
| **Not supported in 1.0.0** | **OIDC / OAuth2** — servers that only allow external login (Google, Keycloak, …) with no local password do **not** work |

Validate your server before use: [`docs/docspell-validation.md`](docs/docspell-validation.md) (section 1.3: local login vs. OIDC).

Release scope and limits: [`docs/release-1.0.0-scope.md`](docs/release-1.0.0-scope.md)

## Installation (release APK)

1. Download the signed APK from the [GitHub release](https://github.com/WinnieThePooh90/docspell_viewer/releases) (`docspell_viewer_1.0.0.apk`).
2. On your device, allow **installation from unknown sources** for the browser or file manager you use (if not already enabled).
3. Open the APK and install.  
   Alternatively via ADB:
   ```bash
   adb install -r docspell_viewer_1.0.0.apk
   ```
4. Start the app → **Account** tab → enter server URL, display name, `collective/user`, and password → save.
5. **Overview** tab: search (leave empty for latest documents or e.g. `*`).

**Note:** Debug and release APKs have different signatures — uninstall a debug build before installing a release APK if needed.

## Features

- **Search & list:** Docspell query, pagination (“Load more”), hit count, correspondent clickable as filter
- **Document:** Detail view, PDF viewer, audio playback, download individual attachments
- **Offline:** Save documents locally and remove them; dedicated offline list tab
- **Favorites:** Mark documents and browse a separate favorites list
- **Filters:** Sidebar (tags, organization/correspondent, category, custom fields, etc.) — visible filters configurable under gear → Settings
- **Settings** (gear icon): Color scheme, dark mode, start page, table size, detail fields, filters, language (DE/EN), **global** cache/offline management, licenses, privacy policy
- **Session:** Automatic login on start; background session refresh and re-login on HTTP 401; **Sync** (top right) as manual fallback
- **Licenses & privacy:** Gear → Settings ([online on GitHub](https://github.com/WinnieThePooh90/docspell_viewer/blob/main/docs/privacy-policy.en.md))

Not included: upload, metadata editing, admin features — see [`docs/mvp-scope.md`](docs/mvp-scope.md).

## Multiple accounts

The app supports **any number of Docspell accounts** (each with its own server URL and login):

- **Account** tab: create, edit, activate, delete accounts; storage for **this account** (offline/cache)
- When **switching accounts**, offline data, favorites, and settings (theme, language, filters, …) are kept **separate per account**
- **Gear → Settings:** storage usage and deletion **across all accounts**

Passwords are stored encrypted on the device and excluded from system backup (see `backup_rules.xml`).

## Authentication (no OIDC)

Only the following is supported:

```
POST /api/v1/open/auth/login
→ token in X-Docspell-Auth header
```

**OIDC-only servers** (local login disabled) are **not compatible** in version 1.0.0. Planned for later releases: clearer error messages and possibly OIDC support (stage B in [`docs/release-checklist.md`](docs/release-checklist.md)).

The app refreshes the session automatically (`POST /sec/auth/session`) and re-logs in with saved account credentials when needed. The **Sync** button forces a fresh login if required.

## Repository contents

| Path | Contents |
|------|----------|
| [`android-blueprint/`](android-blueprint/) | Android project (Gradle) |
| [`docs/docspell-validation.md`](docs/docspell-validation.md) | Server validation against Docspell |
| [`docs/release-checklist.md`](docs/release-checklist.md) | Release checklist stage A / B |
| [`docs/release-1.0.0-scope.md`](docs/release-1.0.0-scope.md) | Version 1.0.0 scope |
| [`docs/mvp-scope.md`](docs/mvp-scope.md) | MVP boundaries (read-first) |
| [`CHANGELOG.md`](CHANGELOG.md) | Version history |
| [`docs/privacy-policy.en.md`](docs/privacy-policy.en.md) | Privacy policy (English) |
| [`docs/privacy-policy.md`](docs/privacy-policy.md) | Privacy policy (German) |

## Run the app (development)

1. In Android Studio: open the `android-blueprint/` folder, Gradle sync
2. Select emulator or device, run the app
3. **Account** tab → save server URL, account, password
4. **Overview** tab → search

Build via CLI:

```bash
cd android-blueprint
./gradlew assembleDebug
```

## Release build (signed APK)

Prerequisite: release keystore and `keystore.properties` (see [`scripts/create_release_keystore.sh`](scripts/create_release_keystore.sh) and `android-blueprint/keystore.properties.example`).

```bash
cd android-blueprint
./gradlew assembleRelease
```

| | |
|--|--|
| **APK filename** | `docspell_viewer_<versionName>.apk` (currently **`docspell_viewer_1.0.0.apk`**) |
| **Artifact path** | `android-blueprint/app/build/outputs/apk/release/docspell_viewer_1.0.0.apk` |
| **Naming in Gradle** | `android-blueprint/app/build.gradle.kts` (`appVersionName` → filename) |

The release build uses R8 (code shrinking) and the release signature from `keystore.properties`. The APK is **not** stored in the Git repo (`.gitignore`: `*.apk`); attach it manually from the build folder for GitHub releases.

Install on device (uninstall debug app first if needed — different signature):

```bash
adb install -r android-blueprint/app/build/outputs/apk/release/docspell_viewer_1.0.0.apk
```

## Version

Current: **1.0.0** (`versionCode` 1) — see [`CHANGELOG.md`](CHANGELOG.md) and `android-blueprint/app/build.gradle.kts`

## License

The **source code of this app** (repository [`docspell_viewer`](https://github.com/WinnieThePooh90/docspell_viewer)) is licensed under the **[Apache License 2.0](LICENSE)** (`SPDX: Apache-2.0`).

Copyright © 2026 Karsten Paulokat

- Full license text: [`LICENSE`](LICENSE) in the repository root
- In the app: **Gear → Settings → Licenses**

**Exception — launcher icons:** App icons are from [Docspell](https://github.com/eikek/docspell) (artwork) and are licensed under **GNU AGPL v3+**, not Apache. Details and source reference are in the app under Licenses and in [`ThirdPartyNotices.kt`](android-blueprint/app/src/main/java/paulokat/de/docspellviewer/ThirdPartyNotices.kt).

Third-party libraries (AndroidX, Kotlin, Retrofit, Moshi, Coil, …) are mostly Apache 2.0; attribution in the app and in `ThirdPartyNotices.kt`.

## Support

Enjoying the app? Want to support my work? I would really appreciate it :)

[![Buy Me A Coffee](https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png)](https://buymeacoffee.com/KarstenPaulokat)
