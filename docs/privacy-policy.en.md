# Privacy Policy — Docspell Viewer

Last updated: **2026-06-02**  
App: **Docspell Viewer** (`paulokat.de.docspellviewer`)  
Controller (app): **Karsten Paulokat**  
Email: **karsten@paulokat.de**  
GitHub Issues: https://github.com/WinnieThePooh90/docspell_viewer/issues

**Online version:** https://github.com/WinnieThePooh90/docspell_viewer/blob/main/docs/privacy-policy.md

German version: [privacy-policy.md](privacy-policy.md)

---

## Summary

Docspell Viewer is a client for **your own Docspell server**. The app sends data **only** to the server you configure. There is **no advertising**, **no tracking**, and **no analytics** by the app developer.

## Data processed

| Data | Purpose | Storage |
|------|---------|---------|
| Server URL | Connect to your Docspell server | Local (account metadata) |
| Account name (`collective/user`) | Server login | Local (account metadata) |
| Password | Login / session renewal | Local, **encrypted** (`EncryptedSharedPreferences`); **excluded from backup** |
| Session token | API access | Memory only |
| Documents, metadata, attachments, previews | Display, search, offline use | Local (cache / offline storage) when used |
| Favorites | In-app list | Local, per account |
| App settings (language, theme, filters, …) | Personalization | Local, per account |

## Where data goes

- Network traffic **only** to the **Docspell server URL** you enter (HTTPS).
- The app developer does **not** operate backend servers and does **not** receive your documents or credentials.
- **No** transmission to ad networks or developer-run analytics.

## Docspell server and data processing roles

**Docspell Viewer** is **client software** on your device. The app developer does not operate a Docspell instance and has no access to your documents on the server.

The app forwards login and API requests to the **server URL you configure**. That server is usually operated by **you** (self-hosting) or your **employer / IT provider** — not by this app’s developer.

There is **no data processing agreement** (GDPR Art. 28) between you and the app developer for your Docspell document content: the developer only provides the app, does not store your documents in a developer-run cloud, and does not receive their contents.

Storage, backup, and access control **on the Docspell server** are the responsibility of whoever **operates that server**. For self-hosting, that is typically you; your Docspell installation’s privacy information applies **separately** from this app notice.

## Buy Me a Coffee (optional)

Settings may open [buymeacoffee.com/KarstenPaulokat](https://buymeacoffee.com/KarstenPaulokat) in your browser. This is a **third-party service** with its own privacy policy. The app does **not** send Docspell data there.

## Backup, deletion, permissions

- **Passwords** are excluded from Android cloud backup (`backup_rules.xml`, `data_extraction_rules.xml`).
- Other app data may be included in Google backup / device transfer depending on device settings.
- Offline data and cache can be cleared in **Settings**; **uninstalling** removes all local app data.
- Permission: **INTERNET** (server access). No location, contacts, or microphone permissions.

## Your rights

You choose server and account. You can remove accounts and local data in the app or uninstall at any time.

Privacy questions: **karsten@paulokat.de** or [GitHub Issues](https://github.com/WinnieThePooh90/docspell_viewer/issues).

## Changes

This notice may change with app updates. In the app: **Settings → Privacy**. Source text also in `PrivacyPolicy.kt`.
