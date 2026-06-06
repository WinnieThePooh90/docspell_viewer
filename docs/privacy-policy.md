# Datenschutzerklärung — Docspell Viewer

Stand: **2026-06-02**  
App: **Docspell Viewer** (`paulokat.de.docspellviewer`)  
Verantwortlicher (App): **Karsten Paulokat**  
E-Mail: **karsten@paulokat.de**  
GitHub Issues: https://github.com/WinnieThePooh90/docspell_viewer/issues

**Online-Version:** https://github.com/WinnieThePooh90/docspell_viewer/blob/main/docs/privacy-policy.md

Englische Fassung: [privacy-policy.en.md](privacy-policy.en.md)

---

## Kurzfassung

Docspell Viewer ist ein Client für **Ihren eigenen Docspell-Server**. Die App sendet Daten **nur** an den von Ihnen konfigurierten Server. Es gibt **keine Werbung**, **kein Tracking** und **keine Analytics** durch den App-Entwickler.

## Welche Daten die App verarbeitet

| Daten | Zweck | Speicherung |
|--------|--------|-------------|
| Server-URL | Verbindung zu Ihrem Docspell-Server | Lokal (Kontometadaten) |
| Kontoname (`collective/user`) | Anmeldung am Server | Lokal (Kontometadaten) |
| Passwort | Anmeldung / Session-Erneuerung | Lokal, **verschlüsselt** (`EncryptedSharedPreferences`); **aus Backup ausgeschlossen** |
| Sitzungs-Token | API-Zugriff | Nur Arbeitsspeicher |
| Dokumente, Metadaten, Anhänge, Vorschaubilder | Anzeige, Suche, Offline-Nutzung | Lokal (Cache / Offline-Speicher) bei Nutzung |
| Favoriten | Merkliste in der App | Lokal, pro Konto |
| App-Einstellungen (Sprache, Theme, Filter, …) | Personalisierung | Lokal, pro Konto |

## Wohin Daten gehen

- Netzwerkverkehr **ausschließlich** an die von Ihnen eingetragene **Docspell-Server-URL** (HTTPS).
- Der App-Entwickler betreibt **keine** Backend-Server und erhält **keine** Dokumente oder Zugangsdaten.
- **Keine** Übermittlung an Werbenetzwerke oder Analyse-Dienste des Entwicklers.

## Docspell-Server und Auftragsverarbeitung

**Docspell Viewer** ist reine **Client-Software** auf Ihrem Gerät. Der App-Entwickler betreibt keine Docspell-Instanz und hat keinen Zugriff auf Ihre Dokumente auf dem Server.

Die App leitet Anmeldung und API-Anfragen an die **von Ihnen eingetragene Server-URL** weiter. Betreiber dieses Docspell-Servers sind in der Regel **Sie selbst** (Self-Hosting) oder Ihr **Arbeitgeber / IT-Dienstleister** — nicht der Entwickler dieser App.

**Keine Auftragsverarbeitung (Art. 28 DSGVO)** zwischen Ihnen und dem App-Entwickler für Ihre Docspell-Dokumente: Der Entwickler stellt nur die App bereit, speichert Ihre Dokumente nicht in einer entwicklergeführten Cloud und empfängt deren Inhalte nicht.

Für Speicherung, Backup und Zugriffsrechte **auf dem Docspell-Server** ist der jeweilige **Server-Betreiber** verantwortlich. Bei Self-Hosting sind das in der Regel Sie; die Datenschutzinformationen Ihrer Docspell-Installation gelten **getrennt** von dieser App-Erklärung.

## Buy Me a Coffee (freiwillig)

In den Einstellungen können Sie optional [buymeacoffee.com/KarstenPaulokat](https://buymeacoffee.com/KarstenPaulokat) im Browser öffnen. Das ist ein **externer Dienst** mit eigener Datenschutzerklärung. Die App übergibt dabei **keine** Docspell-Daten — es wird nur der Browser geöffnet.

## Backup, Löschung, Berechtigungen

- **Passwörter** sind vom Android-Cloud-Backup ausgeschlossen (`backup_rules.xml`, `data_extraction_rules.xml`).
- Andere App-Daten (Kontometadaten, Einstellungen, Offline-Daten, Favoriten) können je nach Geräteeinstellung in Google-Backup / Geräteübertragung einfließen.
- Offline-Daten und Cache können in den **Einstellungen** gelöscht werden; **Deinstallation** entfernt alle lokalen App-Daten.
- Berechtigung: **INTERNET** (Serverzugriff). Keine Standort-, Kontakt- oder Mikrofon-Berechtigungen.

## Ihre Rechte

Sie wählen Server und Konto selbst. Konten und lokale Daten können Sie jederzeit in der App entfernen oder die App deinstallieren.

Fragen zum Datenschutz: **karsten@paulokat.de** oder [GitHub Issues](https://github.com/WinnieThePooh90/docspell_viewer/issues).

## Änderungen

Diese Erklärung kann bei App-Updates angepasst werden. In der App: **Einstellungen → Datenschutz**. Quelltext auch in `PrivacyPolicy.kt`.
