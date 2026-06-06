package paulokat.de.docspellviewer

object DownloadProgress {
    fun percent(bytesRead: Long, contentLength: Long): Int? {
        if (contentLength <= 0L) {
            return null
        }
        return ((bytesRead * 100) / contentLength).toInt().coerceIn(0, 100)
    }

    /** Prozent für die UI: exakt bei bekannter Größe, sonst grobe Schätzung bis 95 %. */
    fun displayPercent(bytesRead: Long, contentLength: Long): Int {
        percent(bytesRead, contentLength)?.let { return it }
        if (bytesRead <= 0L) {
            return 0
        }
        return ((bytesRead / 65_536) + 1).toInt().coerceIn(1, 95)
    }

    fun combinedPercent(
        fileIndex: Int,
        fileCount: Int,
        bytesRead: Long,
        contentLength: Long
    ): Int {
        if (fileCount <= 0) {
            return 0
        }
        val fileFraction = if (contentLength > 0L) {
            bytesRead.toDouble() / contentLength.toDouble()
        } else {
            displayPercent(bytesRead, contentLength) / 100.0
        }
        return ((fileIndex + fileFraction) / fileCount * 100.0)
            .toInt()
            .coerceIn(0, 99)
    }
}
