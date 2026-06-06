# Release 1.0.0 — Scope & Vorbereitung (A1)

Stand: Stufe A abgeschlossen (Sideload / GitHub-Release).

## Release-Ziel

**Docspell Viewer 1.0.0** ist ein **Read-first-Client** für selbst betriebene Docspell-Instanzen:

- Dokumente suchen, anzeigen, offline speichern, favorisieren
- Mehrere Konten (Server/Login) in einer App
- Kein Play-Store-Zwang in Stufe A; Auslieferung als signierte APK

## Navigation (App)

| Bereich | Erreichbar über |
|---------|------------------|
| Login, Konten verwalten | Tab **Konto** (Footer) |
| Suche, Dokumentliste, Sidebar-Filter | Tab **Übersicht** |
| Darstellung, Sprache, Cache gesamt, Lizenzen, Datenschutz | **Zahnrad** → **Einstellungen** |

## Authentifizierung (festgelegt)

| Unterstützt | Nicht in 1.0.0 |
|-------------|----------------|
| Lokaler Docspell-Login (`collective/user` + Passwort) | **OIDC / OAuth2** (z. B. „Login mit Google/Keycloak“) |
| Token via `X-Docspell-Auth` nach `POST /api/v1/open/auth/login` | Share-Links / Gast-Zugang |
| Session-Refresh (`POST /sec/auth/session`) und Re-Login bei 401 | |
| Mehrere gespeicherte Konten | |

Server mit **deaktiviertem lokalem Login** (nur OIDC) sind **nicht kompatibel**. Vor dem Einsatz prüfen: `docs/docspell-validation.md`, Abschnitt 1.3.

## Docspell-Server-Version

- **API:** Docspell REST **v1** (`…/api/v1/`).
- **Referenz:** OpenAPI-Beispiel in `docspell-validation.md` nennt **Docspell 0.43.0**; die App nutzt Standard-Endpunkte (`open/auth/login`, `sec/auth/session`, `sec/item/search`, `sec/item/{id}`, …).
- **Empfehlung:** Gegen die **konkrete Server-Version** des Zielsystems validieren (Checkliste `docspell-validation.md`). Neuere 0.4x-Versionen sollten bei unveränderter v1-API funktionieren; **nicht garantiert** ohne Test.

## App-Version (festgelegt)

| Feld | Wert | Hinweis |
|------|------|---------|
| `versionName` | **1.0.0** | Sichtbar in Einstellungen / APK-Name |
| `versionCode` | **1** | Bei künftigen veröffentlichten APKs erhöhen |
| `applicationId` | `paulokat.de.docspellviewer` | unverändert |

Definiert in `android-blueprint/app/build.gradle.kts`.

## In 1.0.0 enthalten (über MVP hinaus)

- Signierter **Release-Build** mit R8 (Stufe A4)
- **Sprache DE/EN** pro Konto
- **Automatischer Session-Refresh** (`DocspellSessionManager`) und Re-Login bei HTTP 401
- **Datenschutzerklärung** in App und Repository (`docs/privacy-policy.md`)
- Thumbnail-Neuladen nach Session-Erneuerung

## Bewusst nicht in 1.0.0 (Scope)

Siehe auch `docs/mvp-scope.md`:

- Upload von Dateien
- Bearbeiten von Metadaten, Tags, Ordnern
- Admin-Funktionen
- OIDC / externer Identity Provider
- Play Store / F-Droid (Stufe B, optional)

## Bekannte Limitierungen (keine Blocker für 1.0.0-Sideload)

| Thema | Verhalten / Workover |
|--------|----------------------|
| Session komplett ungültig (Passwort geändert, Server weg) | Fehlermeldung; manuell **Sync** oder Konto in Tab **Konto** prüfen |
| Self-signed HTTPS | Kann je nach Gerätezertifikat scheitern |
| OIDC-only-Server | Nicht unterstützt |

## Offene Bugs

**Keine als release-blockierend offen** (Stand Abschluss Stufe A).

| ID | Beschreibung | Status |
|----|--------------|--------|
| — | — | — |
