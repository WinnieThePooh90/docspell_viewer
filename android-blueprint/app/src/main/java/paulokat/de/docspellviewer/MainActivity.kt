package paulokat.de.docspellviewer

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.Coil
import paulokat.de.docspellviewer.ui.DocspellApp

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrapWithStoredLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appCtx = applicationContext
        val accountStore = AccountStore(appCtx)
        val tokenStore = InMemoryTokenStore()
        val sessionManager = DocspellSessionManager(tokenStore = tokenStore)
        Coil.setImageLoader(DocspellImageLoader.create(appCtx, tokenStore, sessionManager))

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppViewModel(
                    appCtx,
                    accountStore,
                    tokenStore,
                    sessionManager
                ) as T
            }
        }

        setContent {
            val vm: AppViewModel = viewModel(factory = viewModelFactory)
            LaunchedEffect(vm) {
                vm.localeChangeEvent.collect {
                    recreate()
                }
            }
            DocspellApp(viewModel = vm)
        }
    }
}
