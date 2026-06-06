package paulokat.de.docspellviewer

import android.app.Application
import android.content.Context

class DocspellApplication : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(AppLocale.wrapWithStoredLanguage(base))
    }
}
