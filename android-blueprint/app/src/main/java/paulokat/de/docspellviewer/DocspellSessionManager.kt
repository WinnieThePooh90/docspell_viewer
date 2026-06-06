package paulokat.de.docspellviewer

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

/**
 * Tracks session lifetime ([LoginResponse.validMs]) and refreshes tokens proactively
 * or after HTTP 401 (via [DocspellApiFactory] authenticator).
 */
class DocspellSessionManager(
    private val tokenStore: TokenStore,
    private var credentialsProvider: () -> ServerSettings? = { null },
    var onSessionUpdated: () -> Unit = {}
) {
    private val refreshMutex = Mutex()

    @Volatile
    private var expiresAtEpochMs: Long = 0L

    @Volatile
    private var activeApiBaseUrl: String? = null

    fun bindCredentials(provider: () -> ServerSettings?) {
        credentialsProvider = provider
    }

    fun bindApiBaseUrl(baseUrl: String) {
        activeApiBaseUrl = normalizeBaseUrl(baseUrl)
    }

    fun applySession(response: LoginResponse) {
        val token = response.token?.takeIf { it.isNotBlank() } ?: return
        tokenStore.setToken(token)
        val validMs = response.validMs?.takeIf { it > 0L } ?: DEFAULT_VALID_MS
        expiresAtEpochMs = System.currentTimeMillis() + validMs
        onSessionUpdated()
    }

    fun clearSession() {
        tokenStore.clear()
        expiresAtEpochMs = 0L
    }

    fun shouldRefreshProactively(): Boolean {
        if (tokenStore.getToken().isNullOrBlank()) {
            return false
        }
        if (expiresAtEpochMs <= 0L) {
            return false
        }
        return System.currentTimeMillis() >= expiresAtEpochMs - REFRESH_BEFORE_EXPIRY_MS
    }

    fun millisUntilProactiveRefresh(): Long? {
        if (tokenStore.getToken().isNullOrBlank() || expiresAtEpochMs <= 0L) {
            return null
        }
        val targetMs = expiresAtEpochMs - REFRESH_BEFORE_EXPIRY_MS
        return (targetMs - System.currentTimeMillis()).coerceAtLeast(MIN_SCHEDULE_DELAY_MS)
    }

    suspend fun refreshProactively(): Boolean {
        if (!shouldRefreshProactively()) {
            return true
        }
        val baseUrl = activeApiBaseUrl ?: credentialsProvider()?.baseUrl ?: return false
        return refreshMutex.withLock {
            if (!shouldRefreshProactively()) {
                return true
            }
            tryRefreshSession(baseUrl) || false
        }
    }

    suspend fun recoverFromUnauthorized(requestBaseUrl: String? = null): Boolean {
        val baseUrl = requestBaseUrl?.let(::normalizeBaseUrl)
            ?: activeApiBaseUrl
            ?: credentialsProvider()?.baseUrl?.let(::normalizeBaseUrl)
            ?: return false

        return refreshMutex.withLock {
            tryRefreshSession(baseUrl) || tryPasswordLogin(baseUrl)
        }
    }

    private suspend fun tryRefreshSession(baseUrl: String): Boolean {
        val api = DocspellApiFactory.createPlain(baseUrl, tokenStore)
        val response = runCatching { api.refreshSession() }.getOrNull() ?: return false
        return acceptSessionResponse(response)
    }

    private suspend fun tryPasswordLogin(baseUrl: String): Boolean {
        val settings = credentialsProvider() ?: return false
        val api = DocspellApiFactory.createPlain(baseUrl, tokenStore)
        val body = """
            {"account":"${escapeJson(settings.account)}","password":"${escapeJson(settings.password)}"}
        """.trimIndent()
            .toRequestBody("application/json; charset=utf-8".toMediaType())
        val response = runCatching { api.login(body) }.getOrNull() ?: return false
        return acceptSessionResponse(response)
    }

    private fun acceptSessionResponse(response: LoginResponse): Boolean {
        if (!response.success || response.token.isNullOrBlank()) {
            return false
        }
        applySession(response)
        return true
    }

    private companion object {
        const val DEFAULT_VALID_MS = 8 * 60 * 60 * 1000L
        const val REFRESH_BEFORE_EXPIRY_MS = 5 * 60 * 1000L
        const val MIN_SCHEDULE_DELAY_MS = 30_000L
    }
}

private fun normalizeBaseUrl(baseUrl: String): String {
    return if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
}

private fun escapeJson(value: String): String {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}

suspend fun <T> DocspellSessionManager.withUnauthorizedRetry(
    baseUrl: String,
    block: suspend () -> T
): T {
    return try {
        block()
    } catch (err: HttpException) {
        if (err.code() != 401 || !recoverFromUnauthorized(baseUrl)) {
            throw err
        }
        block()
    }
}
