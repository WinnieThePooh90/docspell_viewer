package paulokat.de.docspellviewer

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

data class DocspellAccount(
    val id: String,
    val displayName: String,
    val baseUrl: String,
    val account: String,
    val password: String
) {
    fun toServerSettings(): ServerSettings = ServerSettings(
        baseUrl = baseUrl,
        account = account,
        password = password
    )
}

/** @deprecated Use [DocspellAccount]; kept for API/repository compatibility. */
data class ServerSettings(
    val baseUrl: String,
    val account: String,
    val password: String
)

data class AccountListItem(
    val id: String,
    val displayName: String,
    val account: String,
    val serverLabel: String,
    val isActive: Boolean
)

/** Account metadata without password — eligible for system backup. */
private data class StoredAccount(
    val id: String,
    val displayName: String,
    val baseUrl: String,
    val account: String
) {
    fun toDocspellAccount(password: String): DocspellAccount {
        return DocspellAccount(
            id = id,
            displayName = displayName,
            baseUrl = baseUrl,
            account = account,
            password = password
        )
    }
}

class AccountStore(context: Context) {
    // In Application.attachBaseContext applicationContext is not set yet — use base context.
    private val appContext = context.applicationContext ?: context
    private val metadataPrefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val secretsPrefs = openSecretsPrefs(appContext)
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val storedListType = Types.newParameterizedType(
        List::class.java,
        StoredAccount::class.java
    )
    private val storedListAdapter = moshi.adapter<List<StoredAccount>>(storedListType)
    private val legacyListType = Types.newParameterizedType(
        List::class.java,
        DocspellAccount::class.java
    )
    private val legacyListAdapter = moshi.adapter<List<DocspellAccount>>(legacyListType)

    init {
        migrateFromEncryptedCombinedStorageIfNeeded()
        migrateLegacySingleAccountIfNeeded()
    }

    fun list(): List<DocspellAccount> = loadAccounts()

    fun getActive(): DocspellAccount? {
        val activeId = metadataPrefs.getString(KEY_ACTIVE_ID, null) ?: return null
        return getById(activeId)
    }

    fun getById(id: String): DocspellAccount? {
        return loadAccounts().firstOrNull { it.id == id }
    }

    fun setActive(id: String) {
        require(getById(id) != null) { "Unknown account id: $id" }
        metadataPrefs.edit().putString(KEY_ACTIVE_ID, id).apply()
    }

    fun upsert(account: DocspellAccount): DocspellAccount {
        val accounts = loadAccounts().toMutableList()
        val index = accounts.indexOfFirst { it.id == account.id }
        val normalized = account.copy(
            baseUrl = account.baseUrl.trim(),
            account = account.account.trim(),
            displayName = account.displayName.trim().ifBlank {
                defaultDisplayName(account.account, account.baseUrl)
            }
        )
        if (index >= 0) {
            accounts[index] = normalized
        } else {
            accounts.add(normalized)
            if (metadataPrefs.getString(KEY_ACTIVE_ID, null) == null) {
                metadataPrefs.edit().putString(KEY_ACTIVE_ID, normalized.id).apply()
            }
        }
        saveAccounts(accounts)
        return normalized
    }

    fun delete(id: String) {
        val accounts = loadAccounts().filter { it.id != id }
        saveAccounts(accounts)
        deletePassword(id)
        val activeId = metadataPrefs.getString(KEY_ACTIVE_ID, null)
        if (activeId == id) {
            val nextActive = accounts.firstOrNull()?.id
            if (nextActive != null) {
                metadataPrefs.edit().putString(KEY_ACTIVE_ID, nextActive).apply()
            } else {
                metadataPrefs.edit().remove(KEY_ACTIVE_ID).apply()
            }
        }
    }

    fun createNewDraft(): DocspellAccount {
        return DocspellAccount(
            id = UUID.randomUUID().toString(),
            displayName = "",
            baseUrl = defaults.baseUrl,
            account = "",
            password = ""
        )
    }

    fun toListItems(): List<AccountListItem> {
        val activeId = metadataPrefs.getString(KEY_ACTIVE_ID, null)
        return loadAccounts().map { account ->
            AccountListItem(
                id = account.id,
                displayName = account.displayName,
                account = account.account,
                serverLabel = serverLabel(account.baseUrl),
                isActive = account.id == activeId
            )
        }
    }

    private fun loadAccounts(): List<DocspellAccount> {
        val json = metadataPrefs.getString(KEY_ACCOUNTS, null) ?: return emptyList()
        val stored = runCatching { storedListAdapter.fromJson(json) }.getOrNull().orEmpty()
        return stored.map { entry ->
            entry.toDocspellAccount(loadPassword(entry.id))
        }
    }

    private fun saveAccounts(accounts: List<DocspellAccount>) {
        val stored = accounts.map { account ->
            savePassword(account.id, account.password)
            StoredAccount(
                id = account.id,
                displayName = account.displayName,
                baseUrl = account.baseUrl,
                account = account.account
            )
        }
        val json = storedListAdapter.toJson(stored)
        metadataPrefs.edit().putString(KEY_ACCOUNTS, json).apply()
    }

    private fun passwordKey(accountId: String): String = "password_$accountId"

    private fun loadPassword(accountId: String): String {
        return secretsPrefs.getString(passwordKey(accountId), "").orEmpty()
    }

    private fun savePassword(accountId: String, password: String) {
        secretsPrefs.edit().putString(passwordKey(accountId), password).apply()
    }

    private fun deletePassword(accountId: String) {
        secretsPrefs.edit().remove(passwordKey(accountId)).apply()
    }

    private fun migrateFromEncryptedCombinedStorageIfNeeded() {
        val existingJson = metadataPrefs.getString(KEY_ACCOUNTS, null)
        if (existingJson != null) {
            val alreadyMigrated = runCatching { storedListAdapter.fromJson(existingJson) }.isSuccess
            if (alreadyMigrated) {
                return
            }
        }
        val encryptedPrefs = runCatching {
            openEncryptedPrefs(appContext, PREFS_NAME)
        }.getOrNull() ?: return
        val json = encryptedPrefs.getString(KEY_ACCOUNTS, null) ?: return
        val accounts = runCatching { legacyListAdapter.fromJson(json) }.getOrNull().orEmpty()
        if (accounts.isEmpty()) {
            return
        }
        saveAccounts(accounts)
        val activeId = encryptedPrefs.getString(KEY_ACTIVE_ID, null)
        if (activeId != null) {
            metadataPrefs.edit().putString(KEY_ACTIVE_ID, activeId).apply()
        }
        encryptedPrefs.edit().clear().apply()
    }

    private fun migrateLegacySingleAccountIfNeeded() {
        if (metadataPrefs.contains(KEY_ACCOUNTS)) {
            return
        }
        val legacyPrefs = runCatching {
            openEncryptedPrefs(appContext, LEGACY_PREFS_NAME)
        }.getOrNull() ?: return
        val baseUrl = legacyPrefs.getString(LEGACY_KEY_BASE_URL, null)?.trim().orEmpty()
        val account = legacyPrefs.getString(LEGACY_KEY_ACCOUNT, null)?.trim().orEmpty()
        val password = legacyPrefs.getString(LEGACY_KEY_PASSWORD, null).orEmpty()
        if (baseUrl.isBlank() || account.isBlank() || password.isBlank()) {
            return
        }
        val accountId = UUID.randomUUID().toString()
        val docspellAccount = DocspellAccount(
            id = accountId,
            displayName = defaultDisplayName(account, baseUrl),
            baseUrl = baseUrl,
            account = account,
            password = password
        )
        saveAccounts(listOf(docspellAccount))
        metadataPrefs.edit().putString(KEY_ACTIVE_ID, accountId).apply()
        legacyPrefs.edit().clear().apply()
        AccountDataMigration.migrateLegacyScopedData(appContext, accountId)
    }

    companion object {
        private const val TAG = "AccountStore"
        /** Account metadata (no passwords) — included in system backup. */
        const val PREFS_NAME = "docspell_accounts"
        /** Passwords only — excluded from system backup via backup_rules.xml. */
        const val SECRETS_PREFS_NAME = "docspell_account_secrets"

        private fun openSecretsPrefs(context: Context): SharedPreferences {
            return runCatching {
                openEncryptedPrefs(context, SECRETS_PREFS_NAME)
            }.getOrElse { error ->
                Log.w(TAG, "Encrypted password storage unavailable, using private prefs", error)
                context.getSharedPreferences(SECRETS_PREFS_NAME, Context.MODE_PRIVATE)
            }
        }

        private fun openEncryptedPrefs(context: Context, name: String): SharedPreferences {
            return EncryptedSharedPreferences.create(
                context,
                name,
                MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        private const val KEY_ACCOUNTS = "accounts_json"
        private const val KEY_ACTIVE_ID = "active_account_id"

        private const val LEGACY_PREFS_NAME = "docspell_server_settings"
        private const val LEGACY_KEY_BASE_URL = "base_url"
        private const val LEGACY_KEY_ACCOUNT = "account"
        private const val LEGACY_KEY_PASSWORD = "password"

        val defaults = ServerSettings(
            baseUrl = "https://example.org/api/v1/",
            account = "",
            password = ""
        )

        fun defaultDisplayName(account: String, baseUrl: String): String {
            val host = serverLabel(baseUrl)
            return if (host.isNotBlank()) "$account @ $host" else account
        }

        fun serverLabel(baseUrl: String): String {
            return runCatching {
                val uri = Uri.parse(baseUrl.trim())
                uri.host?.takeIf { it.isNotBlank() } ?: baseUrl.trim()
            }.getOrDefault(baseUrl.trim())
        }
    }
}
