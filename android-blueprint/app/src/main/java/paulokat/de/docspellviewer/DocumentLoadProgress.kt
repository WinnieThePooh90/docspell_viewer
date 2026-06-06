package paulokat.de.docspellviewer

/**
 * Fortschritt für Dokument-Ladevorgänge (Download + Anzeige).
 * Wächst nur monoton, damit die UI nicht bei 0 % hängen bleibt.
 */
object DocumentLoadProgress {
    fun displayPercent(bytesRead: Long, contentLength: Long): Int {
        DownloadProgress.percent(bytesRead, contentLength)?.let { return it.coerceIn(1, 100) }
        if (bytesRead <= 0L) {
            return 1
        }
        if (contentLength > 0L) {
            return ((bytesRead * 100) / contentLength).toInt().coerceIn(1, 99)
        }
        val megabytes = bytesRead.toDouble() / (1024.0 * 1024.0)
        return (8 + megabytes * 12.0).toInt().coerceIn(1, 95)
    }

    fun monotonic(current: Int?, next: Int): Int {
        return maxOf(current ?: 0, next.coerceIn(0, 100))
    }

    fun renderPercent(downloadPercent: Int, pageIndex: Int, pageCount: Int): Int {
        if (pageCount <= 0) {
            return downloadPercent.coerceIn(0, 99)
        }
        val renderSpan = (100 - downloadPercent).coerceAtLeast(1)
        val renderDone = ((pageIndex + 1) * renderSpan) / pageCount
        return (downloadPercent + renderDone).coerceIn(0, 100)
    }
}
