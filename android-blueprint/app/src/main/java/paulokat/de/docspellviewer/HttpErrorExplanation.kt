package paulokat.de.docspellviewer

import android.content.Context
import androidx.annotation.StringRes
import retrofit2.HttpException

object HttpErrorExplanation {
    fun forThrowable(context: Context, step: String, err: Throwable): String {
        return when (err) {
            is HttpException -> forHttp(context, step, err.code())
            else -> context.getString(
                R.string.error_format,
                step,
                err.message ?: context.getString(R.string.error_unknown)
            )
        }
    }

    fun forHttp(context: Context, step: String, code: Int): String {
        val hintsRes = hintsResFor(code)
        return if (hintsRes == null) {
            "$step: HTTP $code"
        } else {
            context.getString(
                R.string.error_http_format,
                step,
                code,
                context.getString(hintsRes)
            )
        }
    }

    @StringRes
    private fun hintsResFor(code: Int): Int? {
        return when (code) {
            400 -> R.string.http_hint_400
            401 -> R.string.http_hint_401
            403 -> R.string.http_hint_403
            404 -> R.string.http_hint_404
            408 -> R.string.http_hint_408
            429 -> R.string.http_hint_429
            502 -> R.string.http_hint_502
            503 -> R.string.http_hint_503
            504 -> R.string.http_hint_504
            in 500..599 -> R.string.http_hint_5xx
            else -> R.string.http_hint_other
        }
    }
}
