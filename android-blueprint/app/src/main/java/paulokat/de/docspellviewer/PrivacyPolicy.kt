package paulokat.de.docspellviewer

/**
 * Privacy policy text shown in Settings → Privacy and published in [docs/privacy-policy.md].
 */
data class PrivacySection(
    val title: String,
    val body: String
)

object PrivacyPolicy {
    fun sections(language: AppLanguage): List<PrivacySection> {
        return when (language) {
            AppLanguage.GERMAN -> germanSections()
            AppLanguage.ENGLISH -> englishSections()
        }
    }

    private fun germanSections(): List<PrivacySection> = listOf(
        PrivacySection(
            title = "Verantwortlicher",
            body = """
${AppInfo.COPYRIGHT_HOLDER} („Entwickler“ der App)
E-Mail: ${AppInfo.CONTACT_EMAIL}
GitHub Issues: ${AppInfo.CONTACT_ISSUES_URL}
            """.trimIndent()
        ),
        PrivacySection(
            title = "Kurzfassung",
            body = """
${AppInfo.NAME} ist ein Client für Ihren eigenen Docspell-Server. Die App übermittelt Daten ausschließlich an den von Ihnen konfigurierten Server. Es gibt keine Werbung, kein Tracking und keine Analytics durch den Entwickler.
            """.trimIndent()
        ),
        PrivacySection(
            title = "Welche Daten die App verarbeitet",
            body = """
• Server-URL und Anmeldedaten (Kontoname collective/user und Passwort), die Sie in der App hinterlegen — lokal auf dem Gerät gespeichert; Passwörter verschlüsselt (Android EncryptedSharedPreferences)
• Anzeigename und Kontometadaten (ohne Passwort) pro gespeichertem Konto
• Sitzungs-Token nach Anmeldung — nur im Arbeitsspeicher, für API-Anfragen an Ihren Docspell-Server
• Dokumente, Metadaten, Vorschaubilder und Anhänge, die Sie über die App abrufen oder offline speichern
• Favoriten und App-Einstellungen (Sprache, Darstellung, Filter) — pro Konto getrennt
• Viewer- und Thumbnail-Cache auf dem Gerät
            """.trimIndent()
        ),
        PrivacySection(
            title = "Wohin Daten gehen",
            body = """
Netzwerkanfragen gehen nur an die von Ihnen eingetragene Docspell-Server-URL (HTTPS). Der Entwickler betreibt keine eigenen Server und erhält keine Ihrer Dokumente oder Zugangsdaten.

Die App sendet keine Daten an Werbenetzwerke oder Analyse-Dienste des Entwicklers.
            """.trimIndent()
        ),
        PrivacySection(
            title = "Docspell-Server und Auftragsverarbeitung",
            body = """
${AppInfo.NAME} ist reine Client-Software auf Ihrem Gerät. Der App-Entwickler betreibt keine Docspell-Instanz und hat keinen Zugriff auf Ihre Dokumente auf dem Server.

Wenn Sie die App nutzen, leitet sie Anmeldung und API-Anfragen an die von Ihnen eingetragene Server-URL weiter. Betreiber dieses Docspell-Servers sind in der Regel Sie selbst (Self-Hosting) oder Ihr Arbeitgeber bzw. IT-Dienstleister — nicht der Entwickler dieser App.

Eine Auftragsverarbeitung (Art. 28 DSGVO) zwischen Ihnen und dem App-Entwickler findet für Ihre Docspell-Dokumente nicht statt: Der Entwickler stellt nur die App bereit, speichert Ihre Dokumente nicht in einer entwicklergeführten Cloud und empfängt deren Inhalte nicht.

Für die Verarbeitung auf dem Docspell-Server (Speicherung, Backup, Zugriffsrechte) ist der jeweilige Server-Betreiber verantwortlich. Bei Self-Hosting sind das in der Regel Sie; dort gelten die Datenschutzinformationen Ihrer Docspell-Installation getrennt von dieser App-Erklärung.
            """.trimIndent()
        ),
        PrivacySection(
            title = "Buy Me a Coffee (freiwillig)",
            body = """
In den Einstellungen können Sie freiwillig einen Link zu buymeacoffee.com (${SupportLinks.BUY_ME_A_COFFEE_URL}) öffnen. Das ist ein externer Dienst; dort gelten die Datenschutzbestimmungen von Buy Me a Coffee. Die App übermittelt dabei keine Docspell-Daten an diesen Dienst — es wird nur Ihr Browser geöffnet.
            """.trimIndent()
        ),
        PrivacySection(
            title = "Speicherung, Backup und Löschung",
            body = """
Passwörter sind vom Android-System-Backup ausgeschlossen. Andere App-Daten (Kontometadaten, Einstellungen, Offline-Dokumente, Favoriten) können — je nach Geräteeinstellung — in ein Google-Backup oder Geräteübertragung einfließen.

Sie können Offline-Daten und Cache in den Einstellungen löschen. Deinstallation der App entfernt alle lokal gespeicherten App-Daten.
            """.trimIndent()
        ),
        PrivacySection(
            title = "Berechtigungen",
            body = """
Die App nutzt die Berechtigung INTERNET, um Ihren Docspell-Server zu erreichen. Weitere sensible Berechtigungen (Standort, Kontakte, Mikrofon usw.) werden nicht abgefragt.
            """.trimIndent()
        ),
        PrivacySection(
            title = "Ihre Rechte",
            body = """
Sie bestimmen, welcher Server und welches Konto verwendet wird. Sie können Konten und lokale Daten jederzeit in der App entfernen oder die App deinstallieren.

Bei Fragen zum Datenschutz: ${AppInfo.CONTACT_EMAIL} oder GitHub Issues (Link oben).
            """.trimIndent()
        ),
        PrivacySection(
            title = "Änderungen",
            body = """
Stand: ${AppInfo.PRIVACY_POLICY_DATE}. Diese Erklärung kann bei App-Updates angepasst werden.

Aktuelle Fassung online: ${AppInfo.PRIVACY_POLICY_URL}
In der App: Einstellungen → Datenschutz.
            """.trimIndent()
        )
    )

    private fun englishSections(): List<PrivacySection> = listOf(
        PrivacySection(
            title = "Controller",
            body = """
${AppInfo.COPYRIGHT_HOLDER} (developer of the app)
Email: ${AppInfo.CONTACT_EMAIL}
GitHub Issues: ${AppInfo.CONTACT_ISSUES_URL}
            """.trimIndent()
        ),
        PrivacySection(
            title = "Summary",
            body = """
${AppInfo.NAME} is a client for your own Docspell server. The app sends data only to the server you configure. There is no advertising, no tracking, and no analytics by the developer.
            """.trimIndent()
        ),
        PrivacySection(
            title = "Data processed by the app",
            body = """
• Server URL and login credentials (collective/user account name and password) you enter — stored locally; passwords encrypted (Android EncryptedSharedPreferences)
• Display name and account metadata (without password) for each saved account
• Session token after login — in memory only, for API requests to your Docspell server
• Documents, metadata, previews, and attachments you fetch or save for offline use
• Favorites and app settings (language, appearance, filters) — per account
• Viewer and thumbnail cache on device
            """.trimIndent()
        ),
        PrivacySection(
            title = "Where data goes",
            body = """
Network requests go only to the Docspell server URL you enter (HTTPS). The developer does not operate backend servers and does not receive your documents or credentials.

The app does not send data to ad networks or developer-run analytics services.
            """.trimIndent()
        ),
        PrivacySection(
            title = "Docspell server and data processing roles",
            body = """
${AppInfo.NAME} is client software on your device. The app developer does not operate a Docspell instance and has no access to your documents on the server.

When you use the app, it forwards login and API requests to the server URL you configure. That Docspell server is usually operated by you (self-hosting) or by your employer or IT provider — not by this app’s developer.

There is no data processing agreement (GDPR Art. 28) between you and the app developer for your Docspell document content: the developer only provides the app, does not store your documents in a developer-run cloud, and does not receive their contents.

Processing on the Docspell server (storage, backup, access control) is the responsibility of whoever operates that server. For self-hosting, that is typically you; the privacy information for your Docspell installation applies separately from this app notice.
            """.trimIndent()
        ),
        PrivacySection(
            title = "Buy Me a Coffee (optional)",
            body = """
Settings may link to buymeacoffee.com (${SupportLinks.BUY_ME_A_COFFEE_URL}). That is a third-party service with its own privacy policy. The app does not send Docspell data there — it only opens your browser.
            """.trimIndent()
        ),
        PrivacySection(
            title = "Storage, backup, and deletion",
            body = """
Passwords are excluded from Android cloud backup. Other app data (account metadata, settings, offline documents, favorites) may be included in Google backup or device transfer depending on your device settings.

You can delete offline data and cache in Settings. Uninstalling the app removes all locally stored app data.
            """.trimIndent()
        ),
        PrivacySection(
            title = "Permissions",
            body = """
The app uses the INTERNET permission to reach your Docspell server. It does not request location, contacts, microphone, or similar sensitive permissions.
            """.trimIndent()
        ),
        PrivacySection(
            title = "Your rights",
            body = """
You choose which server and account to use. You can remove accounts and local data in the app at any time or uninstall the app.

For privacy questions: ${AppInfo.CONTACT_EMAIL} or GitHub Issues (link above).
            """.trimIndent()
        ),
        PrivacySection(
            title = "Changes",
            body = """
Last updated: ${AppInfo.PRIVACY_POLICY_DATE}. This notice may change with app updates.

Current version online: ${AppInfo.PRIVACY_POLICY_URL}
In the app: Settings → Privacy.
            """.trimIndent()
        )
    )
}
