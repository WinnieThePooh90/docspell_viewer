package paulokat.de.docspellviewer

object DocspellUrls {
    private fun base(apiBaseUrl: String): String = apiBaseUrl.trim().trimEnd('/')

    fun itemPreview(apiBaseUrl: String, itemId: String): String =
        "${base(apiBaseUrl)}/sec/item/$itemId/preview?withFallback=true"

    fun attachmentView(apiBaseUrl: String, attachmentId: String): String =
        "${base(apiBaseUrl)}/sec/attachment/$attachmentId/view"

    fun attachmentDownload(apiBaseUrl: String, attachmentId: String): String =
        "${base(apiBaseUrl)}/sec/attachment/$attachmentId/original"

    /** PDF-Ausgabe des Anhangs (konvertiert, falls noetig). */
    fun attachmentPdf(apiBaseUrl: String, attachmentId: String): String =
        "${base(apiBaseUrl)}/sec/attachment/$attachmentId"

    fun webHost(apiBaseUrl: String): String {
        val uri = java.net.URI(base(apiBaseUrl))
        return "${uri.scheme}://${uri.host}"
    }
}
