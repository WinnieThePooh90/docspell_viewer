# MVP-Scope (finalisiert)

Dieser Scope setzt To-do 3 um.

## Release 1: Read-first

- Login via `POST /api/v1/open/auth/login`
- Token-Refresh via `POST /api/v1/sec/auth/session`
- Dokumentsuche via `GET/POST /api/v1/sec/item/search`
- Dokumentdetail via `GET /api/v1/sec/item/{id}`
- Attachment-Anzeige via Preview/Original-Endpunkte aus `/api/doc`

## Nicht in Release 1

- Upload von Dateien
- Bearbeiten von Metadaten, Tags, Foldern
- Admin-Endpunkte
- Share-Authentifizierung

## Warum diese Entscheidung

- Schnellster Weg zu einer stabilen mobilen Nutzung fuer Lesen/Suchen.
- Upload und Edit erhoehen Komplexitaet bei Berechtigungen, Konflikten und UX.
- Kernnutzen fuer mobile Nutzung wird bereits in Release 1 erreicht.
