# Android-Blueprint (To-do 4)

## Ziel

Ein klares Startgeruest fuer eine Docspell-Android-App mit sauberer Trennung von UI, Netzwerk und Auth.

## Struktur

- `android-blueprint/settings.gradle.kts`
- `android-blueprint/build.gradle.kts`
- `android-blueprint/app/build.gradle.kts`
- `android-blueprint/app/src/main/AndroidManifest.xml`
- `android-blueprint/app/src/main/java/paulokat/de/docspellviewer/MainActivity.kt`
- `android-blueprint/app/src/main/java/paulokat/de/docspellviewer/DocspellApi.kt`
- Application ID: `paulokat.de.docspellviewer`

## API/Flow

1. Nutzer loggt sich ein (`/open/auth/login`).
2. Token wird im `TokenStore` gehalten (im Blueprint in-memory; produktiv verschluesselt speichern).
3. OkHttp-Interceptor setzt `X-Docspell-Auth` bei Requests.
4. Suche laedt Dokumente (`/sec/item/search`).
5. Detailansicht nutzt Item-ID (`/sec/item/{id}`).

## Produktions-Hinweise

- `InMemoryTokenStore` durch sichere Speicherung ersetzen (z. B. Jetpack Security).
- Credentials niemals im Code lassen.
- Timeout, Retry und 401-Refresh robust ausbauen.
- Attachment-Endpunkte exakt aus deiner `/api/doc`-Version uebernehmen.
