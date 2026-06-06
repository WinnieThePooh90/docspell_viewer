# Release 1.0.0 — Scope & Vorbereitung (A1)

Stand: Vorbereitung Stufe-A-Release (Sideload / GitHub).

## Release-Ziel

**Docspell Viewer 1.0.0** ist ein **Read-first-Client** für selbst betriebene Docspell-Instanzen:

- Dokumente suchen, anzeigen, offline speichern, favorisieren
- Mehrere Konten (Server/Login) in einer App
- Kein Play-Store-Zwang in Stufe A; Auslieferung als signierte APK

## Authentifizierung (festgelegt)

| Unterstützt | Nicht in 1.0.0 |
|-------------|----------------|
| Lokaler Docspell-Login (`collective/user` + Passwort) | **OIDC / OAuth2** (z. B. „Login mit Google/Keycloak“) |
| Token via `X-Docspell-Auth` nach `POST /api/v1/open/auth/login` | Share-Links / Gast-Zugang |
| Mehrere gespeicherte Konten | |

Server mit **deaktiviertem lokalem Login** (nur OIDC) sind **nicht kompatibel**. Vor dem Einsatz prüfen: `docs/docspell-validation.md`, Abschnitt 1.3.

## Docspell-Server-Version

- **API:** Docspell REST **v1** (`…/api/v1/`).
- **Referenz:** OpenAPI-Beispiel in `docspell-validation.md` nennt **Docspell 0.43.0**; die App nutzt Standard-Endpunkte (`open/auth/login`, `sec/item/search`, `sec/item/{id}`, …).
- **Empfehlung vor Release:** Gegen die **konkrete Server-Version** des Zielsystems validieren (Checkliste `docspell-validation.md`). Neuere 0.4x-Versionen sollten bei unveränderter v1-API funktionieren; **nicht garantiert** ohne Test.

## App-Version (festgelegt)

| Feld | Wert | Hinweis |
|------|------|---------|
| `versionName` | **1.0.0** | Sichtbar in Einstellungen / APK-Name |
| `versionCode` | **1** | Bei jeder veröffentlichten APK erhöhen (auch Bugfix-Releases) |
| `applicationId` | `paulokat.de.docspellviewer` | unverändert |

Definiert in `android-blueprint/app/build.gradle.kts`.

## Bewusst nicht in 1.0.0 (Scope)

Siehe auch `docs/mvp-scope.md`:

- Upload von Dateien
- Bearbeiten von Metadaten, Tags, Ordnern
- Admin-Funktionen
- OIDC / externer Identity Provider

Zusätzlich in der App **noch nicht umgesetzt** (für **1.1+** / Stufe B vorgesehen):

- Automatischer **Session-Refresh** (`POST /sec/auth/session`); bei abgelaufenem Token: erneuter Login aus gespeicherten Kontodaten nach App-Neustart bzw. manuell „Sync“
- Release-Build mit R8/Signierung (Stufe A4)
- Datenschutzseite / Play Store (Stufe B)

## Bekannte Limitierungen (keine Blocker für 1.0.0-Sideload)

| Thema | Verhalten / Workaround |
|--------|-------------------------|
| Abgelaufene Session (lange Laufzeit ohne Neustart) | „Sync“ oder App neu starten (Re-Login aus gespeichertem Konto) |
| Self-signed HTTPS | Kann je nach Zertifikat am Gerät scheitern; Vertrauenswürdigkeit des Zertifikats am Android-Gerät nötig |
| `security-crypto` | Alpha-Dependency; für Stufe A akzeptiert, Stabilisierung in späterem Release |
| Nur Deutsch in der UI | Keine Übersetzungen in 1.0.0 |

## Offene Bugs

**Keine als Release-blockierend erfasst** (Stand A1). Neue Funde vor Veröffentlichung hier eintragen:

| ID | Beschreibung | Release 1.0.0 |
|----|--------------|-----------------|
| — | — | — |
