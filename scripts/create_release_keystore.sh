#!/usr/bin/env bash
# Erzeugt den Release-Keystore für Docspell Viewer (A4, Schritt 1).
# Außerhalb des Git-Repos — Passwörter werden interaktiv abgefragt.
set -euo pipefail

KEYTOOL="${KEYTOOL:-/opt/android-studio/jbr/bin/keytool}"
KEYS_DIR="${KEYS_DIR:-$HOME/Nextcloud/Programmierung/Keys/docspell_viewer}"
KEYSTORE="$KEYS_DIR/docspell_viewer_release.jks"
ALIAS="docspell_viewer"
VALIDITY_DAYS=10000

if [[ ! -x "$KEYTOOL" ]]; then
  echo "Fehler: keytool nicht gefunden unter $KEYTOOL" >&2
  echo "Setze KEYTOOL=/pfad/zum/keytool oder installiere Android Studio JDK." >&2
  exit 1
fi

mkdir -p "$KEYS_DIR"
chmod 700 "$KEYS_DIR"

if [[ -f "$KEYSTORE" ]]; then
  echo "Keystore existiert bereits:"
  echo "  $KEYSTORE"
  echo "Nicht überschreiben — bei Verlust sind App-Updates unmöglich."
  exit 1
fi

echo "=== Release-Keystore: Docspell Viewer ==="
echo ""
echo "Speicherort: $KEYSTORE"
echo "Alias:       $ALIAS"
echo "Gültigkeit:  ${VALIDITY_DAYS} Tage (~27 Jahre)"
echo ""
echo "Wähle ein starkes Passwort und speichere es im Passwort-Manager."
echo "Ohne Passwort + Keystore-Datei keine Updates mehr möglich!"
echo ""

read -rsp "Keystore-Passwort: " STORE_PASS
echo
read -rsp "Keystore-Passwort wiederholen: " STORE_PASS2
echo
if [[ "$STORE_PASS" != "$STORE_PASS2" ]]; then
  echo "Fehler: Passwörter stimmen nicht überein." >&2
  exit 1
fi
if [[ ${#STORE_PASS} -lt 6 ]]; then
  echo "Fehler: Passwort muss mindestens 6 Zeichen haben." >&2
  exit 1
fi

read -rsp "Key-Passwort (Enter = gleich wie Keystore): " KEY_PASS
echo
if [[ -z "$KEY_PASS" ]]; then
  KEY_PASS="$STORE_PASS"
fi

read -rp "Name (CN, z. B. Max Mustermann): " CN
CN="${CN:-Docspell Viewer}"
read -rp "Organisation (O, optional): " ORG
ORG="${ORG:-}"
read -rp "Land (C, 2 Buchstaben, z. B. DE): " C
C="${C:-DE}"

DNAME="CN=$CN"
if [[ -n "$ORG" ]]; then
  DNAME="$DNAME, O=$ORG"
fi
DNAME="$DNAME, C=$C"

echo ""
echo "Erstelle Keystore …"

"$KEYTOOL" -genkeypair -v \
  -keystore "$KEYSTORE" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity "$VALIDITY_DAYS" \
  -storetype PKCS12 \
  -storepass "$STORE_PASS" \
  -keypass "$KEY_PASS" \
  -dname "$DNAME"

chmod 600 "$KEYSTORE"

PROPS="$KEYS_DIR/keystore.properties"
cat > "$PROPS" <<EOF
storeFile=$KEYSTORE
storePassword=$STORE_PASS
keyAlias=$ALIAS
keyPassword=$KEY_PASS
EOF
chmod 600 "$PROPS"

echo ""
echo "Fertig."
echo "  Keystore:    $KEYSTORE"
echo "  Properties:  $PROPS"
echo ""
echo "Backup: Kopiere beide Dateien an einen zweiten sicheren Ort"
echo "(Passwort-Manager + USB oder verschlüsseltes Backup)."
echo ""
echo "Nächster Schritt (A4.2): Gradle signingConfigs einrichten —"
echo "Verweis auf $PROPS oder Kopie nach android-blueprint/keystore.properties"
