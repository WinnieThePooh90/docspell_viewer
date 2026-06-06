# Docspell-Validierung fuer Android-App

Diese Datei setzt die To-dos 1 und 2 aus dem Plan konkret um.

## 1) Server-Version und Auth-Methode verifizieren

### 1.1 OpenAPI-Version pruefen

```bash
curl -fsSL "https://docspell.org/openapi/docspell-openapi.html" | rg "Docspell \\("
```

Erwartung: Ausgabe mit einer Versionsangabe wie `Docspell (0.43.0)`.

### 1.2 Laufenden Server pruefen

```bash
curl -i "https://<DEIN-DOCSPELL-HOST>/api/doc"
```

Erwartung: HTTP 200 (oder Login-Seite/API-Doku ueber Reverse Proxy).

### 1.3 Auth-Modus pruefen (lokal vs OIDC)

Docspell unterstuetzt:

- lokales Login ueber `POST /api/v1/open/auth/login`
- optional OIDC/OAuth2-Provider

Pruefschritt lokal:

```bash
curl -sS -X POST "https://<DEIN-DOCSPELL-HOST>/api/v1/open/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"account":"<COLLECTIVE/USER>","password":"<PASSWORT>"}'
```

Erwartung: JSON mit `success`, `token`, `validMs`.

Wenn lokales Login deaktiviert ist, muss der App-Login als OIDC-Flow umgesetzt werden.

## 2) Kern-Endpunkte gegen laufenden Server testen

Voraussetzung: `TOKEN` aus Login.

```bash
export DOCSPELL_BASE_URL="https://<DEIN-DOCSPELL-HOST>/api/v1"
export DOCSPELL_TOKEN="<TOKEN>"
```

### 2.1 Session-Refresh

```bash
curl -sS -X POST "$DOCSPELL_BASE_URL/sec/auth/session" \
  -H "X-Docspell-Auth: $DOCSPELL_TOKEN"
```

Erwartung: neues `token` + `validMs`.

### 2.2 Dokument-Suche (GET)

```bash
curl -sS "$DOCSPELL_BASE_URL/sec/item/search?q=*&limit=20&offset=0&withDetails=true" \
  -H "X-Docspell-Auth: $DOCSPELL_TOKEN"
```

Erwartung: JSON mit `groups`.

### 2.3 Dokument-Suche (POST)

```bash
curl -sS -X POST "$DOCSPELL_BASE_URL/sec/item/search" \
  -H "Content-Type: application/json" \
  -H "X-Docspell-Auth: $DOCSPELL_TOKEN" \
  -d '{"offset":0,"limit":20,"withDetails":true,"searchMode":"normal","query":"*"}'
```

### 2.4 Item-Details

```bash
curl -sS "$DOCSPELL_BASE_URL/sec/item/<ITEM_ID>" \
  -H "X-Docspell-Auth: $DOCSPELL_TOKEN"
```

Erwartung: JSON mit Metadaten und Anhaengen.

### 2.5 Attachment-Preview/Original

Je nach Inhaltstyp und Endpunktstruktur deiner Serverversion:

```bash
curl -I "$DOCSPELL_BASE_URL/sec/item/<ITEM_ID>/preview"
curl -I "$DOCSPELL_BASE_URL/sec/attachment/<ATTACHMENT_ID>"
```

Wenn 404, bitte den genauen Attachment-/Preview-Endpunkt in `/api/doc` nachschlagen und den Request entsprechend anpassen.

## 3) Ergebnis-Matrix (zum Ausfuellen)

- Login ok: [ ]
- Session-Refresh ok: [ ]
- Search GET ok: [ ]
- Search POST ok: [ ]
- Item-Details ok: [ ]
- Attachment/Preview ok: [ ]
- OIDC erforderlich: [ ] ja / [ ] nein

## 4) Ableitung fuer den App-Scope

Falls die Matrix erfolgreich ist, ist ein Read-first-MVP unmittelbar umsetzbar:

- Login + Token-Refresh
- Search + Paging
- Detail + Attachment-Viewer

Upload/Bearbeitung kann danach iterativ folgen.
