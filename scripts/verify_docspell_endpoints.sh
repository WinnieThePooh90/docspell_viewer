#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${DOCSPELL_BASE_URL:-}" || -z "${DOCSPELL_ACCOUNT:-}" || -z "${DOCSPELL_PASSWORD:-}" ]]; then
  echo "Bitte setzen:"
  echo "  DOCSPELL_BASE_URL (z.B. https://host/api/v1)"
  echo "  DOCSPELL_ACCOUNT (z.B. collective/user)"
  echo "  DOCSPELL_PASSWORD"
  exit 1
fi

echo "== Login =="
LOGIN_JSON="$(curl -fsS -X POST "${DOCSPELL_BASE_URL}/open/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"account\":\"${DOCSPELL_ACCOUNT}\",\"password\":\"${DOCSPELL_PASSWORD}\"}")"
echo "${LOGIN_JSON}" | rg "\"success\":true" >/dev/null
TOKEN="$(echo "${LOGIN_JSON}" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')"
if [[ -z "${TOKEN}" ]]; then
  echo "Kein Token im Login-Response."
  exit 1
fi
echo "Login OK"

echo "== Session Refresh =="
REFRESH_JSON="$(curl -fsS -X POST "${DOCSPELL_BASE_URL}/sec/auth/session" \
  -H "X-Docspell-Auth: ${TOKEN}")"
echo "${REFRESH_JSON}" | rg "\"success\":true" >/dev/null
echo "Refresh OK"

echo "== Search GET =="
SEARCH_JSON="$(curl -fsS "${DOCSPELL_BASE_URL}/sec/item/search?q=*&limit=5&offset=0&withDetails=true" \
  -H "X-Docspell-Auth: ${TOKEN}")"
echo "${SEARCH_JSON}" | rg "\"groups\"" >/dev/null
echo "Search OK"

echo "== Ergebnis =="
echo "Kern-Endpunkte erfolgreich verifiziert."
