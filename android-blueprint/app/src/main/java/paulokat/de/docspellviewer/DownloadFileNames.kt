package paulokat.de.docspellviewer

object DownloadFileNames {
    fun sanitize(name: String): String {
        val trimmed = name.trim().ifBlank { "dokument" }
        val cleaned = trimmed.replace(Regex("[\\\\/:*?\"<>|]"), "_")
        return if (cleaned.contains('.')) cleaned else "$cleaned.pdf"
    }

    fun guessMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            "txt" -> "text/plain"
            else -> "application/octet-stream"
        }
    }

    private val audioExtensions = setOf("mp3", "wav", "ogg", "m4a", "aac", "flac", "opus", "webm")

    fun isAudioFileName(fileName: String): Boolean {
        val ext = extension(fileName)
        return ext in audioExtensions
    }

    fun extension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
            .takeIf { it.matches(Regex("[a-z0-9]{1,8}")) } ?: "bin"
    }
}
