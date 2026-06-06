# Release-Checkliste Docspell Viewer

Geordnete Checklisten für einen **öffentlichen Release** in zwei Stufen.  
**Stufe A** zuerst vollständig abhaken, dann **Stufe B** (baut darauf auf).

Legende: `[ ]` offen · `[x]` erledigt

---

## Stufe A — Sideload / GitHub-Release (1.0.0)

Ziel: Signierte Release-APK, aktuelle Doku, keine offensichtlichen Sicherheits- oder Rechtslücken. Kein Play Store nötig.

### A1 — Vorbereitung & Scope

- [x] Release-Ziel festlegen: nur **lokaler Docspell-Login** (kein OIDC) — in README erwähnen
- [x] Ziel-Docspell-Version notieren (z. B. aus `docs/docspell-validation.md`)
- [x] `versionName` / `versionCode` in `app/build.gradle.kts` prüfen und für diesen Release festlegen
- [x] Offene Bugs für diesen Release sammeln (oder bewusst auf nächste Version verschieben)

### A2 — Funktion manuell prüfen

Reihenfolge: erst frische Installation, dann Upgrade-Szenario (falls möglich).

- [x] Checkliste `docs/docspell-validation.md` komplett gegen euren Test-Server durchgehen
- [x] Erstinstallation ohne Konto → Einstellungen → Konto anlegen → Login & Suche
- [x] Dokumentliste, „Weitere laden“, Detail, PDF öffnen
- [x] Offline speichern / löschen; Favorit setzen / entfernen
- [x] Zweites Konto anlegen, **Konto wechseln**, Daten (Offline/Favoriten) getrennt prüfen
- [x] Einstellungen: Cache/Offline **Benutzer** vs. **Gesamt** (Anzeige + Löschen)
- [x] App beenden & neu starten → automatische Anmeldung / Zustand ok
- [x] Flugmodus / Server aus → verständliche Fehlermeldungen
- [x] Einstellungen → **Lizenzen** öffnet und Inhalt vollständig lesbar

### A3 — Sicherheit (Minimum)

- [x] `backup_rules.xml` + ggf. `data_extraction_rules.xml`: Passwörter vom System-Backup ausschließen (Rest inkl. Kontometadaten, Einstellungen, Offline, Favoriten)
- [x] `AndroidManifest.xml`: `android:allowBackup` nur mit diesen Regeln belassen (oder bewusst dokumentieren)
- [ ] Prüfen: kein Http-Logging / keine Passwörter in Logcat im Release-Build
- [x] `androidx.security:security-crypto` — Version prüfen (Alpha vs. stabile Release-Version) → `1.1.0` stabil

### A4 — Release-Build

- [x] Release-Keystore anlegen und **sicher** ablegen (nicht ins Git)
- [x] `signingConfigs` + `buildTypes { release { ... } }` in `app/build.gradle.kts` einrichten
- [x] `minifyEnabled` / R8 für Release aktivieren (Start: `true`, bei Problemen Regeln nachziehen)
- [x] `./gradlew assembleRelease` erfolgreich
- [x] Release-APK auf echtem Gerät installieren und **A2** kurz wiederholen (Smoke)
- [x] APK-Name / Artefakt-Pfad dokumentieren → [`README.md`](../README.md#release-build-signierte-apk)

**Release-Artefakt (1.0.0):**

| | |
|--|--|
| Dateiname | `docspell_viewer_1.0.0.apk` |
| Pfad nach `./gradlew assembleRelease` | `android-blueprint/app/build/outputs/apk/release/docspell_viewer_1.0.0.apk` |
| Benennung | `app/build.gradle.kts` — `docspell_viewer_$appVersionName.apk` |

### A5 — Dokumentation & Recht (Repository)

- [x] `LICENSE` im Repo-Root (Lizenz für **euren** App-Code, z. B. Apache-2.0)
- [x] `README.md` aktualisieren: Features, Multi-Account, Voraussetzungen, Installation, kein OIDC
- [x] `CHANGELOG.md` anlegen mit Eintrag **1.0.0**
- [x] In README: Link zu `docs/docspell-validation.md` und `docs/release-checklist.md`
- [x] Hinweis zu Docspell-Icons / AGPL in README oder Verweis auf In-App-Lizenzen

### A6 — Veröffentlichung Stufe A

- [x] Git-Tag `v1.0.0` (optional, aber empfohlen)
- [x] Release-APK als GitHub-Release anhängen (oder anderer Kanal) — Draft mit APK
- [x] Release Notes aus `CHANGELOG.md` übernehmen → Vorlage [`docs/github-release-notes-1.0.0.md`](github-release-notes-1.0.0.md)
- [ ] Kurz testen: Download + Installation der veröffentlichten APK (nach **Publish** des Drafts)

**Stufe A abgeschlossen, wenn A1–A6 durchgehend erledigt sind.**

**Noch offen für A6-Abschluss:** Draft auf GitHub veröffentlichen (Release Notes einfügen, Tag `v1.0.0` wählen, **Publish release**), dann APK vom Release herunterladen und auf Gerät installieren.

---

## Stufe B — Öffentlicher Store-Release (Play / F-Droid)

Ziel: Längere Nutzung, Store-Richtlinien, Datenschutz, wartbarer Build-Prozess. **Erst starten, wenn Stufe A fertig ist.**

### B1 — Session & Robustheit

- [ ] `validMs` / Ablauf des Tokens analysieren (Login-Response)
- [ ] `refreshSession()` implementieren oder bei 401 automatisch Re-Login aus gespeicherten Kontodaten
- [ ] Verhalten bei abgelaufenem Token in allen Hauptflows testen (Suche, Detail, PDF, Offline)
- [ ] Optional: Hinweis in UI „Session abgelaufen — bitte erneut anmelden“

### B2 — Kompatibilität & Grenzen

- [ ] Mit **zwei Docspell-Versionen** testen (falls möglich: ältere + aktuelle)
- [ ] Server **ohne lokales Login** (nur OIDC): klare Meldung statt kryptischem Fehler
- [ ] Self-signed / internes HTTPS: Verhalten dokumentieren (Zertifikat, Nutzerhinweis)
- [ ] Große PDFs / viele Offline-Dokumente: Speicher & Performance stichprobenartig

### B3 — Qualitätssicherung & Automatisierung

- [ ] Mindestens wenige **Unit-Tests** (z. B. Query-Normalizer, URL-Builder, Migration)
- [ ] Optional: ein **Instrumentierungs-Smoke-Test** (App startet, Einstellungen öffnen)
- [ ] CI (z. B. GitHub Actions): `./gradlew assembleRelease` + Tests bei jedem Push/PR
- [ ] Interne Regression-Checkliste aus A2 als Vorlage für Test-Releases nutzen

### B4 — Datenschutz & Transparenz

- [ ] Datenschutzerklärung schreiben (URL, statische Seite oder Repo-`docs/`)
- [ ] Inhalt: welche Daten (Server-URL, Account, Passwort lokal, Offline-Cache, keine Weitergabe an Dritte außer Docspell-Server)
- [ ] Buy-Me-a-Coffee-Link: in Datenschutz erwähnen (externer Dienst)
- [ ] In App verlinken: Einstellungen → Datenschutz (optional, für Store oft Pflicht)

### B5 — Google Play (falls Ziel = Play Store)

- [ ] Developer-Konto / App-Eintrag anlegen
- [ ] **Data safety** Formular ausfüllen (Datenarten, Verschlüsselung, Löschung)
- [ ] Store-Listing: Kurz- & Langbeschreibung (Deutsch, ggf. Englisch)
- [ ] Screenshots (Phone), Feature-Grafik, App-Icon hochladen
- [ ] Content-Rating-Fragebogen
- [ ] `versionCode` für jeden Upload erhöhen
- [ ] AAB (`bundleRelease`) bauen und in Play Console testen (Internal testing → Closed → Production)
- [ ] Datenschutz-URL in Store-Eintrag eintragen

### B6 — F-Droid (falls Ziel = F-Droid)

- [ ] Reproduzierbarer Build nachvollziehbar (Tags, Gradle, keine proprietären Blobs)
- [ ] Metadaten (Summary, Description, Kategorien, License, Source Code Link)
- [ ] Merge Request / Einreichung beim F-Droid-Repo
- [ ] Hinweis: AGPL-Artwork / Lizenzen konsistent mit `ThirdPartyNotices`

### B7 — Wartung nach Release

- [ ] Prozess für Bugfix-Releases (`versionCode`++, CHANGELOG, Tag)
- [ ] Kanal für Nutzer-Feedback (Issue-Tracker, E-Mail)
- [ ] Bekannte Limitationen in README pflegen (OIDC, Upload, Metadaten-Edit)

**Stufe B abgeschlossen, wenn die gewählten Store-Ziele (B5 und/oder B6) plus B1–B4 erledigt sind.**

---

## Empfohlene Gesamtreihenfolge (Kurz)

```text
A1 Scope → A2 Testen → A3 Sicherheit → A4 Release-Build → A5 Doku → A6 Veröffentlichen
    ↓
B1 Session → B2 Kompatibilität → B3 Tests/CI → B4 Datenschutz → B5 Play und/oder B6 F-Droid → B7 Wartung
```

---

## Schnellreferenz: wichtige Dateien

| Thema | Pfad |
|--------|------|
| Release 1.0.0 Scope (A1) | `docs/release-1.0.0-scope.md` |
| Server-Validierung | `docs/docspell-validation.md` |
| MVP-Scope | `docs/mvp-scope.md` |
| Build / Version | `android-blueprint/app/build.gradle.kts` |
| App-Lizenz | `LICENSE` (Apache-2.0) |
| Changelog | `CHANGELOG.md` |
| GitHub Release Notes (1.0.0) | `docs/github-release-notes-1.0.0.md` |
| Release-APK (Artefakt) | `android-blueprint/app/build/outputs/apk/release/docspell_viewer_1.0.0.apk` |
| ProGuard / R8 | `android-blueprint/app/proguard-rules.pro` |
| Release-Keystore (lokal) | `~/Nextcloud/Programmierung/Keys/docspell_viewer/` |
| Manifest / Backup | `android-blueprint/app/src/main/AndroidManifest.xml` |
| Lizenzen (in App) | `ThirdPartyNotices.kt`, `ui/LicensesScreen.kt` |
| API / Session | `DocspellApi.kt`, `AppViewModel.kt` |
