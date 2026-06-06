# Docspell Viewer (Android)

Android-App (**Kotlin + Compose**) zum Lesen und Durchsuchen von Dokumenten auf einem **eigenen Docspell-Server** über die REST-API v1.

## Voraussetzungen

- Docspell-Server mit erreichbarer URL `https://<host>/api/v1/`
- **Lokaler Account-Login** (`collective/user` + Passwort) auf dem Server aktiv  
- **Nicht unterstützt in 1.0.0:** OIDC/OAuth2 (Login nur über externen Provider ohne lokales Passwort)

Details zum Release-Scope: [`docs/release-1.0.0-scope.md`](docs/release-1.0.0-scope.md)

## Funktionen (Auszug)

- Suche und Dokumentliste (mit Nachladen)
- Detailansicht, PDF-Viewer, Audio-Anhänge, Download
- Offline-Speicherung, Favoriten, mehrere Konten
- Einstellungen (Theme, Felder, Cache/Offline pro Konto und gesamt)
- Open-Source-Hinweise in der App (Lizenzen)

## Inhalt des Repositories

- [`android-blueprint/`](android-blueprint/) — Android-Projekt
- [`docs/docspell-validation.md`](docs/docspell-validation.md) — Validierung gegen einen Docspell-Server
- [`docs/release-checklist.md`](docs/release-checklist.md) — Release-Checkliste Stufe A / B
- [`docs/release-1.0.0-scope.md`](docs/release-1.0.0-scope.md) — Scope und Version 1.0.0
- [`docs/mvp-scope.md`](docs/mvp-scope.md) — MVP-Grenzen (Read-first)

## App starten (Entwicklung)

1. In Android Studio: `android-blueprint/` öffnen, Gradle Sync
2. Emulator oder Gerät wählen, App starten
3. Einstellungen → Konto: Server-URL, Account, Passwort speichern
4. Startseite: Suche (z. B. leer für neueste Dokumente oder `*`)

Build per CLI: `./gradlew assembleDebug` im Ordner `android-blueprint/` (Gradle Wrapper vorhanden).

## Version

Aktuell: **1.0.0** (`versionCode` 1) — siehe `android-blueprint/app/build.gradle.kts`
