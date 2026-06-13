package paulokat.de.docspellviewer

import android.app.Application
import android.content.Context
import coil.Coil

class DocspellApplication : Application() {
    val tokenStore: TokenStore = InMemoryTokenStore()
    lateinit var sessionManager: DocspellSessionManager
        private set

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLocale.wrapWithStoredLanguage(base))
    }

    override fun onCreate() {
        super.onCreate()
        sessionManager = DocspellSessionManager(tokenStore = tokenStore)
        Coil.setImageLoader(DocspellImageLoader.create(this, tokenStore, sessionManager))
    }
}
