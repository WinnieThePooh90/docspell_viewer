package paulokat.de.docspellviewer

interface TokenStore {
    fun getToken(): String?
    fun setToken(token: String)
    fun clear()
}

class InMemoryTokenStore : TokenStore {
    private var token: String? = null

    override fun getToken(): String? = token
    override fun setToken(token: String) {
        this.token = token
    }
    override fun clear() {
        token = null
    }
}
