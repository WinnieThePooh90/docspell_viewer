package paulokat.de.docspellviewer

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object AppLocale {
    fun wrap(context: Context, language: AppLanguage): Context {
        val locale = Locale.forLanguageTag(language.tag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun wrapWithStoredLanguage(context: Context): Context {
        return wrap(context, resolveAppLanguage(context))
    }

    fun resolveAppLanguage(context: Context): AppLanguage {
        val appContext = context.applicationContext ?: context
        val activeAccount = AccountStore(appContext).getActive()
        if (activeAccount != null) {
            return AppPreferencesStore(appContext, activeAccount.id).load().appLanguage
        }
        return GlobalPreferencesStore(context).getLanguage()
    }
}
