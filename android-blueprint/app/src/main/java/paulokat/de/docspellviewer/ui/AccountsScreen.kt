package paulokat.de.docspellviewer.ui

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import paulokat.de.docspellviewer.R
import paulokat.de.docspellviewer.SettingsUiState

@Composable
fun AccountsScreenContent(
    state: SettingsUiState,
    onRefresh: () -> Unit,
    onSelectAccount: (String) -> Unit,
    onAddAccount: () -> Unit,
    onDeleteAccount: () -> Unit,
    onActivateAccount: (String) -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onShowPasswordChange: (Boolean) -> Unit,
    onSaveAccount: () -> Unit,
    onClearUserOfflineData: () -> Unit,
    onClearUserViewerCache: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onRefresh()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.accounts_delete_title)) },
            text = {
                Text(stringResource(R.string.accounts_delete_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text(stringResource(R.string.accounts_delete_confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.accounts_cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FeedbackBanner(
            message = state.saveMessage,
            isError = state.saveMessageIsError
        )

        Text(
            text = stringResource(R.string.accounts_title),
            style = MaterialTheme.typography.titleMedium
        )
        state.accounts.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectAccount(item.id) }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (item.isActive) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.account} · ${item.serverLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (item.isActive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.accounts_active),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    OutlinedButton(onClick = { onActivateAccount(item.id) }) {
                        Text(stringResource(R.string.accounts_activate))
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onAddAccount, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.accounts_add))
            }
            if (state.editingAccountId != null && state.accounts.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.accounts_delete))
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text(
            text = if (state.isNewAccount) stringResource(R.string.accounts_new) else stringResource(R.string.accounts_edit),
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = state.displayName,
            onValueChange = onDisplayNameChange,
            label = { Text(stringResource(R.string.accounts_display_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.baseUrl,
            onValueChange = onBaseUrlChange,
            label = { Text(stringResource(R.string.accounts_server_url)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.account,
            onValueChange = onAccountChange,
            label = { Text(stringResource(R.string.accounts_account)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.accounts_password)) },
            visualTransformation = if (state.showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = state.showPassword,
                onCheckedChange = onShowPasswordChange
            )
            Text(stringResource(R.string.accounts_show_password))
        }

        Button(onClick = onSaveAccount, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.settings_save))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Text(
            text = stringResource(R.string.accounts_account_data),
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = pluralStringResource(R.plurals.accounts_offline_documents, state.offlineDocumentCount, state.offlineDocumentCount),
            style = MaterialTheme.typography.bodyMedium
        )
        AccountStorageStatRow(
            label = stringResource(R.string.settings_offline_storage),
            value = Formatter.formatFileSize(context, state.offlineStorageBytes),
            buttonLabel = stringResource(R.string.settings_delete_data),
            onButtonClick = onClearUserOfflineData
        )
        AccountStorageStatRow(
            label = stringResource(R.string.settings_viewer_cache),
            value = Formatter.formatFileSize(context, state.viewerCacheBytes),
            buttonLabel = stringResource(R.string.settings_clear_cache),
            onButtonClick = onClearUserViewerCache
        )
    }
}


@Composable
private fun AccountStorageStatRow(
    label: String,
    value: String,
    buttonLabel: String,
    onButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$label $value",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        OutlinedButton(onClick = onButtonClick) {
            Text(buttonLabel)
        }
    }
}
