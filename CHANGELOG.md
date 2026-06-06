# Changelog

Alle wesentlichen Änderungen an **Docspell Viewer** werden in dieser Datei dokumentiert.

Format orientiert sich an [Keep a Changelog](https://keepachangelog.com/de/1.1.0/).  
Versionierung folgt [Semantic Versioning](https://semver.org/lang/de/).

## [1.0.0] — 2026-06-02

Erster öffentlicher Release (Stufe A: Sideload / GitHub-Release).

### Hinzugefügt

- **Dokumente lesen:** Suche, Dokumentenliste mit Nachladen, Detailansicht, PDF-Viewer, Audio-Anhänge, Download einzelner Anhänge
- **Offline & Favoriten:** Dokumente offline speichern und löschen; Favoriten markieren; eigene Listen pro Konto
- **Mehrere Konten:** Beliebig viele Docspell-Server/Konten; Tab **Konto** zum Verwalten; getrennte Einstellungen, Offline-Daten und Favoriten je Konto
- **Filter & Navigation:** Sidebar-Filter (Tags, Korrespondent, Kategorie, benutzerdefinierte Felder u. a.); konfigurierbare Startseite; Docspell-Query-Syntax in der Suche
- **Einstellungen** (Zahnrad): Farbschema, Dark Mode, Tabellengröße, Detailfelder, Filter, Sprache (DE/EN) pro Konto, Cache-/Offline-Verwaltung gesamt
- **Session:** Automatischer Login beim Start; proaktiver Session-Refresh (`validMs`); bei HTTP 401 automatisch Refresh oder Re-Login; manueller **Sync** als Fallback
- **Recht & Transparenz:** Apache-2.0; Lizenzen- und Datenschutz-Bildschirm in der App; Datenschutzerklärung auf GitHub

### Nicht enthalten (bewusst)

- OIDC / OAuth2 (Login nur über externen Identity Provider ohne lokales Passwort)
- Upload von Dateien, Bearbeiten von Metadaten, Admin-Funktionen

### Technik

- Kotlin, Jetpack Compose, Material 3
- Min SDK 26, Target SDK 35
- Release-Build mit R8 und signierter APK (`docspell_viewer_1.0.0.apk`)
- Passwortspeicherung: `androidx.security:security-crypto` 1.1.0; Passwörter vom System-Backup ausgeschlossen

### Bekannte Limitierungen

- Server ohne lokalen Login (nur OIDC) werden nicht unterstützt
- Self-signed HTTPS kann je nach Gerätezertifikat fehlschlagen
- Bei dauerhaft ungültigen Zugangsdaten: Fehlermeldung; Konto prüfen oder Sync

[1.0.0]: https://github.com/WinnieThePooh90/docspell_viewer/releases/tag/v1.0.0
