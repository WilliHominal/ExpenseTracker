package com.warh.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warh.commons.TopBarDefault
import com.warh.domain.models.AccountType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun AccountsRoute(
    vm: AccountsViewModel = koinViewModel(),
    onAccountClick: (Long) -> Unit,
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snackBar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopBarDefault(title = stringResource(R.string.accounts_title))
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.startAdd() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) { Icon(Icons.Default.Add, null) } },
        snackbarHost = { SnackbarHost(snackBar) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {

            ui.draft?.let { d ->
                AccountEditorCard(
                    draft = d,
                    onName = vm::onName,
                    onType = vm::onType,
                    onCurrency = vm::onCurrency,
                    onBalanceText = vm::onBalanceText,
                    onCancel = vm::cancelEdit,
                    onSave = { vm.saveEdit { msg -> scope.launch { snackBar.showSnackbar(msg) } } }
                )
                HorizontalDivider()
            }

            LazyColumn(Modifier.fillMaxSize()) {
                items(ui.accounts, key = { it.id }) { acc ->
                    ListItem(
                        headlineContent = { Text(acc.name) },
                        supportingContent = {
                            Text(stringResource(R.string.accounts_item_meta, acc.type, acc.currency))
                        },
                        trailingContent = {
                            Row {
                                TextButton(onClick = { vm.startEdit(acc) }) {
                                    Text(stringResource(R.string.accounts_edit))
                                }
                                IconButton(onClick = {
                                    vm.delete(acc) { msg -> scope.launch { snackBar.showSnackbar(msg) } }
                                }) { Icon(Icons.Default.Delete, null) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { onAccountClick(acc.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountEditorCard(
    draft: AccountDraft,
    onName: (String) -> Unit,
    onType: (AccountType) -> Unit,
    onCurrency: (String) -> Unit,
    onBalanceText: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Card(Modifier.fillMaxWidth().padding(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            OutlinedTextField(
                value = draft.name,
                onValueChange = onName,
                label = { Text(stringResource(R.string.accounts_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Type
            var typeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                OutlinedTextField(
                    value = draft.type.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.accounts_type_label)) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    AccountType.entries.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t.name) },
                            onClick = { onType(t); typeExpanded = false }
                        )
                    }
                }
            }

            // Currency
            val currencies = remember {
                listOf("ARS","USD","EUR","BRL","CLP","UYU","MXN")
            }
            var curExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = curExpanded, onExpandedChange = { curExpanded = it }) {
                OutlinedTextField(
                    value = draft.currency,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.accounts_currency_label)) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = curExpanded, onDismissRequest = { curExpanded = false }) {
                    currencies.forEach { code ->
                        DropdownMenuItem(
                            text = { Text(code) },
                            onClick = { onCurrency(code); curExpanded = false }
                        )
                    }
                }
            }

            // Balance
            OutlinedTextField(
                value = draft.balanceText,
                onValueChange = onBalanceText,
                label = { Text(stringResource(R.string.accounts_initial_balance_label, draft.currency)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                singleLine = true,
                supportingText = { Text(stringResource(R.string.accounts_initial_balance_hint)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                TextButton(onClick = onCancel) { Text(stringResource(R.string.accounts_cancel)) }
                Button(
                    onClick = onSave,
                    enabled = draft.name.isNotBlank()
                ) { Text(stringResource(R.string.accounts_save)) }
            }
        }
    }
}