# Docspell Viewer (Android)

**Sprachen:** **Deutsch** · [English](README.en.md)

Android-App (**Kotlin + Jetpack Compose**) zum Lesen und Durchsuchen von Dokumenten auf einem **eigenen Docspell-Server** über die REST-API v1.

## Voraussetzungen

| | |
|--|--|
| **Android** | 8.0 (API 26) oder höher |
| **Docspell-Server** | Erreichbare URL `https://<host>/api/v1/` |
| **Anmeldung** | **Lokaler Account-Login** (`collective/user` + Passwort) auf dem Server muss aktiv sein |
| **Nicht unterstützt in 1.0.0** | **OIDC / OAuth2** — Server, die nur externen Login (Google, Keycloak, …) erlauben und kein lokales Passwort, funktionieren **nicht** |

Vor dem Einsatz den Server prüfen: [`docs/docspell-validation.md`](docs/docspell-validation.md) (Abschnitt 1.3: lokaler Login vs. OIDC).

Release-Scope und Grenzen: [`docs/release-1.0.0-scope.md`](docs/release-1.0.0-scope.md)

## Installation (Release-APK)

1. Signierte APK aus dem [GitHub-Release](https://github.com/WinnieThePooh90/docspell_viewer/releases) herunterladen (`docspell_viewer_1.0.0.apk`).
2. Auf dem Gerät **Installation aus unbekannten Quellen** für den verwendeten Browser/Dateimanager erlauben (falls noch nicht geschehen).
3. APK öffnen und installieren.  
   Alternativ per ADB:
   ```bash
   adb install -r docspell_viewer_1.0.0.apk
   ```
4. App starten → Tab **Konto** → Server-URL, Anzeigename, `collective/user` und Passwort eintragen → speichern.
5. Tab **Übersicht**: Suche (leer lassen für neueste Dokumente oder z. B. `*`).

**Hinweis:** Debug- und Release-APK haben unterschiedliche Signaturen — vor Installation einer Release-APK ggf. eine Debug-Version deinstallieren.

## Funktionen

- **Suche & Liste:** Docspell-Query, Paginierung („Weitere laden“), Trefferanzahl, Korrespondent als Filter anklickbar
- **Dokument:** Detailansicht, PDF-Viewer, Audio-Wiedergabe, Anhänge herunterladen
- **Offline:** Dokumente lokal speichern und wieder entfernen; Offline-Liste im eigenen Tab
- **Favoriten:** Markieren und gesonderte Favoriten-Liste
- **Filter:** Sidebar (Tags, Organisation/Korrespondent, Kategorie, benutzerdefinierte Felder u. a.) — sichtbare Filter unter Zahnrad → Einstellungen konfigurierbar
- **Einstellungen** (Zahnrad): Farbschema, Dark Mode, Startseite, Tabellengröße, Detailfelder, Filter, Sprache (DE/EN), Cache-/Offline-Verwaltung **gesamt**, Lizenzen, Datenschutz
- **Session:** Automatischer Login beim Start; Session-Refresh im Hintergrund und Re-Login bei 401; **Sync** (oben rechts) als manueller Fallback
- **Lizenzen & Datenschutz:** Zahnrad → Einstellungen ([Online auf GitHub](https://github.com/WinnieThePooh90/docspell_viewer/blob/main/docs/privacy-policy.md))

Nicht enthalten: Upload, Metadaten bearbeiten, Admin-Funktionen — siehe [`docs/mvp-scope.md`](docs/mvp-scope.md).

## Mehrere Konten

Die App unterstützt **beliebig viele Docspell-Konten** (jeweils eigene Server-URL und Login):

- Tab **Konto:** Konten anlegen, bearbeiten, aktivieren, löschen; Speicher **dieses Kontos** (Offline/Cache)
- Beim **Wechsel** werden Offline-Daten, Favoriten und Einstellungen (Theme, Sprache, Filter, …) **pro Konto getrennt** gehalten
- **Zahnrad → Einstellungen:** Speicherplatz und Löschen **gesamt über alle Konten**

Passwörter werden verschlüsselt auf dem Gerät gespeichert und vom System-Backup ausgeschlossen (siehe `backup_rules.xml`).

## Authentifizierung (kein OIDC)

Unterstützt wird ausschließlich:

```
POST /api/v1/open/auth/login
→ Token im Header X-Docspell-Auth
```

**OIDC-only-Server** (lokaler Login deaktiviert) sind in Version 1.0.0 **nicht kompatibel**. Geplant für spätere Releases: klarere Fehlermeldung und ggf. OIDC-Unterstützung (Stufe B in [`docs/release-checklist.md`](docs/release-checklist.md)).

Die App erneuert die Session automatisch (`POST /sec/auth/session`) und meldet sich bei Bedarf mit gespeicherten Kontodaten wieder an. Der **Sync**-Button erzwingt einen erneuten Login, falls nötig.

## Inhalt des Repositories

| Pfad | Inhalt |
|------|--------|
| [`android-blueprint/`](android-blueprint/) | Android-Projekt (Gradle) |
| [`docs/docspell-validation.md`](docs/docspell-validation.md) | Server-Validierung gegen Docspell |
| [`docs/release-checklist.md`](docs/release-checklist.md) | Release-Checkliste Stufe A / B |
| [`docs/release-1.0.0-scope.md`](docs/release-1.0.0-scope.md) | Scope Version 1.0.0 |
| [`docs/mvp-scope.md`](docs/mvp-scope.md) | MVP-Grenzen (Read-first) |
| [`CHANGELOG.md`](CHANGELOG.md) | Versionshistorie |
| [`docs/privacy-policy.md`](docs/privacy-policy.md) | Datenschutzerklärung (DE/EN) |

## App starten (Entwicklung)

1. In Android Studio: Ordner `android-blueprint/` öffnen, Gradle Sync
2. Emulator oder Gerät wählen, App starten
3. Tab **Konto** → Server-URL, Account, Passwort speichern
4. Tab **Übersicht** → Suche

Build per CLI:

```bash
cd android-blueprint
./gradlew assembleDebug
```

## Release-Build (signierte APK)

Voraussetzung: Release-Keystore und `keystore.properties` (siehe [`scripts/create_release_keystore.sh`](scripts/create_release_keystore.sh) und `android-blueprint/keystore.properties.example`).

```bash
cd android-blueprint
./gradlew assembleRelease
```

| | |
|--|--|
| **APK-Dateiname** | `docspell_viewer_<versionName>.apk` (aktuell **`docspell_viewer_1.0.0.apk`**) |
| **Artefakt-Pfad** | `android-blueprint/app/build/outputs/apk/release/docspell_viewer_1.0.0.apk` |
| **Benennung in Gradle** | `android-blueprint/app/build.gradle.kts` (`appVersionName` → Dateiname) |

Release-Build nutzt R8 (Code-Shrinking) und die Release-Signatur aus `keystore.properties`. Die APK liegt **nicht** im Git-Repo (`.gitignore`: `*.apk`); für GitHub-Releases manuell aus dem Build-Ordner anhängen.

Installation auf Gerät (Debug-App ggf. vorher deinstallieren — andere Signatur):

```bash
adb install -r android-blueprint/app/build/outputs/apk/release/docspell_viewer_1.0.0.apk
```

## Version

Aktuell: **1.0.0** (`versionCode` 1) — siehe [`CHANGELOG.md`](CHANGELOG.md) und `android-blueprint/app/build.gradle.kts`

## Lizenz

Der **Quellcode dieser App** (Repository [`docspell_viewer`](https://github.com/WinnieThePooh90/docspell_viewer)) steht unter der **[Apache License 2.0](LICENSE)** (`SPDX: Apache-2.0`).

Copyright © 2026 Karsten Paulokat

- Vollständiger Lizenztext: [`LICENSE`](LICENSE) im Repository-Root
- In der App: **Zahnrad → Einstellungen → Lizenzen**

**Ausnahme — Launcher-Icons:** Die App-Icons stammen von [Docspell](https://github.com/eikek/docspell) (Artwork) und unterliegen der **GNU AGPL v3+**, nicht der Apache-Lizenz. Details und Quellverweis stehen in der App unter Lizenzen sowie in [`ThirdPartyNotices.kt`](android-blueprint/app/src/main/java/paulokat/de/docspellviewer/ThirdPartyNotices.kt).

Drittanbieter-Bibliotheken (AndroidX, Kotlin, Retrofit, Moshi, Coil, …) sind überwiegend Apache 2.0; Attribution in der App und in `ThirdPartyNotices.kt`.

## Unterstützung

Dir gefällt die App? Du möchtest meine Arbeit ein bisschen unterstützen? Darüber würde ich mich sehr freuen :)

[![Buy Me A Coffee](https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png)](https://buymeacoffee.com/KarstenPaulokat)
