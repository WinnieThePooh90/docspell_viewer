# Docspell Viewer 1.0.0

Erster öffentlicher Release — Android-Client für **selbst betriebene Docspell-Server** (Read-first, Sideload via APK).

## Installation

1. **`docspell_viewer_1.0.0.apk`** aus den Release-Assets herunterladen
2. Auf dem Android-Gerät installieren  
   *(Falls bereits eine Debug-Version installiert ist: vorher deinstallieren — andere Signatur)*
3. App starten → Tab **Konto** → Server-URL, Anzeigename, `collective/user` und Passwort speichern
4. Tab **Übersicht** → Suche starten (leer lassen für neueste Dokumente, oder z. B. `*`)

Alternativ per ADB: `adb install -r docspell_viewer_1.0.0.apk`

## Voraussetzungen

| | |
|--|--|
| **Android** | 8.0 (API 26) oder höher |
| **Docspell-Server** | Erreichbare URL `https://<host>/api/v1/` |
| **Login** | **Lokaler Account** (`collective/user` + Passwort) muss auf dem Server aktiv sein |
| **Nicht unterstützt** | **OIDC / OAuth2** — Server mit ausschließlich externem Login (Google, Keycloak, …) funktionieren **nicht** |

Server vorab prüfen: [docspell-validation.md](https://github.com/WinnieThePooh90/docspell_viewer/blob/main/docs/docspell-validation.md) (Abschnitt 1.3)

## Highlights

- **Dokumente lesen:** Suche (Docspell-Query), Liste mit Nachladen und Sortierung, Detailansicht, PDF-Viewer, Audio-Anhänge, Download einzelner Anhänge
- **Offline & Favoriten:** Dokumente lokal speichern und entfernen; Favoriten markieren; eigene Listen pro Konto
- **Mehrere Konten:** Beliebig viele Docspell-Server/Konten; getrennte Einstellungen, Offline-Daten und Favoriten je Konto
- **Filter & Navigation:** Sidebar-Filter (Tags, Korrespondent, Kategorie, benutzerdefinierte Felder u. a.); konfigurierbare Startseite
- **Einstellungen** (Zahnrad): Farbschema, Dark Mode, Tabellengröße, Detailfelder, Filter, Sprache (DE/EN), Cache-/Offline-Verwaltung gesamt
- **Session:** Automatischer Login beim Start; Session-Refresh im Hintergrund; Re-Login bei HTTP 401; **Sync**-Button als manueller Fallback
- **Transparenz:** Datenschutzerklärung und Lizenzen in der App; App-Code unter **Apache 2.0**

## Bewusst nicht enthalten

- OIDC / OAuth2
- Upload von Dateien
- Metadaten bearbeiten
- Admin-Funktionen

## Technik

- Kotlin, Jetpack Compose, Material 3
- Min SDK 26, Target SDK 35
- Release-Build mit R8 und signierter APK
- Passwörter verschlüsselt gespeichert, vom System-Backup ausgeschlossen

## Bekannte Limitierungen

- Server ohne lokalen Login (nur OIDC) werden nicht unterstützt
- Self-signed HTTPS kann je nach Gerätezertifikat fehlschlagen
- Bei dauerhaft ungültigen Zugangsdaten: Fehlermeldung — Konto prüfen oder **Sync** nutzen

## Links

- [README (English)](https://github.com/WinnieThePooh90/docspell_viewer/blob/main/README.md)
- [README (Deutsch)](https://github.com/WinnieThePooh90/docspell_viewer/blob/main/README.de.md)
- [Changelog](https://github.com/WinnieThePooh90/docspell_viewer/blob/main/CHANGELOG.md)
- [Datenschutzerklärung (DE)](https://github.com/WinnieThePooh90/docspell_viewer/blob/main/docs/privacy-policy.md)
- [Privacy Policy (EN)](https://github.com/WinnieThePooh90/docspell_viewer/blob/main/docs/privacy-policy.en.md)

---

**Lizenz:** App-Quellcode [Apache 2.0](https://github.com/WinnieThePooh90/docspell_viewer/blob/main/LICENSE) · Launcher-Icons von Docspell (AGPL v3+)
