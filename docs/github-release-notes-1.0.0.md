# Docspell Viewer 1.0.0

Erster öffentlicher Release (Sideload / GitHub-Release).

## Installation

1. Asset **`docspell_viewer_1.0.0.apk`** herunterladen
2. Auf dem Android-Gerät installieren (ggf. Debug-Version vorher deinstallieren — andere Signatur)
3. Tab **Konto** → Server-URL, Login speichern → Tab **Übersicht**

**Voraussetzung:** Docspell-Server mit **lokalem Login** (`collective/user` + Passwort). **Kein OIDC** in 1.0.0.

## Neu in 1.0.0

- **Dokumente lesen:** Suche, Dokumentenliste mit Nachladen, Detailansicht, PDF-Viewer, Audio-Anhänge, Download einzelner Anhänge
- **Offline & Favoriten:** Dokumente offline speichern und löschen; Favoriten markieren; eigene Listen pro Konto
- **Mehrere Konten:** Beliebig viele Docspell-Server/Konten; Tab **Konto**; getrennte Einstellungen, Offline-Daten und Favoriten je Konto
- **Filter & Navigation:** Sidebar-Filter (Tags, Korrespondent, Kategorie, benutzerdefinierte Felder u. a.); konfigurierbare Startseite
- **Einstellungen** (Zahnrad): Farbschema, Dark Mode, Tabellengröße, Detailfelder, Filter, Sprache (DE/EN), Cache-/Offline-Verwaltung gesamt
- **Session:** Automatischer Login beim Start; Session-Refresh im Hintergrund; Re-Login bei 401; Sync-Button als Fallback
- **Datenschutz & Lizenzen:** Zahnrad → Einstellungen → Datenschutz / Lizenzen; Apache-2.0 für App-Code

## Nicht enthalten

- OIDC / OAuth2
- Upload, Metadaten bearbeiten, Admin-Funktionen

## Technik

- Android 8.0+ (API 26), Target SDK 35
- Kotlin, Jetpack Compose, Material 3

## Bekannte Limitierungen

- Server ohne lokalen Login (nur OIDC) werden nicht unterstützt
- Self-signed HTTPS kann je nach Gerätezertifikat fehlschlagen
- Ungültige Zugangsdaten dauerhaft: Fehlermeldung; Konto prüfen oder Sync

---

Vollständiger Changelog: [`CHANGELOG.md`](../CHANGELOG.md)  
Datenschutz: [`privacy-policy.md`](privacy-policy.md)
