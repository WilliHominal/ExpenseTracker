package com.warh.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warh.commons.NumberUtils
import com.warh.commons.TopBarDefault
import com.warh.commons.color_picker.ColorChooser
import com.warh.domain.models.AccountType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Currency
import com.warh.commons.R.drawable as CommonDrawables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsRoute(
    vm: AccountsViewModel = koinViewModel(),
    onAccountClick: (Long) -> Unit,
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snackBar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val editing = ui.draft != null

    Scaffold(
        topBar = {
            TopBarDefault(title = stringResource(R.string.accounts_title))
        },
        floatingActionButton = {
            if (!editing) {
                FloatingActionButton(
                    onClick = { vm.startAdd() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) { Icon(Icons.Default.Add, null) }
            }
        },
        snackbarHost = { SnackbarHost(snackBar) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                start = padding.calculateStartPadding(LayoutDirection.Ltr) + 12.dp,
                end   = padding.calculateEndPadding(LayoutDirection.Ltr) + 12.dp,
                top   = padding.calculateTopPadding() + 12.dp,
                bottom = padding.calculateBottomPadding()
            )
        ) {
            item { CurrencyTotalsCard(ui.totalsByCurrency, Modifier.fillMaxWidth()) }

            ui.draft?.let { d ->
                item {
                    AccountEditorCard(
                        draft = d,
                        onName = vm::onName,
                        onType = vm::onType,
                        onCurrency = vm::onCurrency,
                        onBalanceText = vm::onBalanceText,
                        onIconIndex = vm::onIconIndex,
                        onIconColor = vm::onIconColor,
                        onCancel = vm::cancelEdit,
                        onSave = { vm.saveEdit { msg -> scope.launch { snackBar.showSnackbar(msg) } } }
                    )
                }
            }

            items(ui.accounts, key = { it.id }) { acc ->
                val iconIds = remember {
                    listOf(
                        CommonDrawables.account_icon_1, CommonDrawables.account_icon_2, CommonDrawables.account_icon_3, CommonDrawables.account_icon_4,
                        CommonDrawables.account_icon_5, CommonDrawables.account_icon_6, CommonDrawables.account_icon_7, CommonDrawables.account_icon_8,
                    )
                }

                ListItem(
                    leadingContent = {
                        val idx = (acc.iconIndex - 1).coerceIn(0, iconIds.lastIndex)
                        val tint = acc.iconColorArgb?.let { Color(it.toInt()) }
                            ?: MaterialTheme.colorScheme.onSurfaceVariant

                        Icon(
                            painter = painterResource(iconIds[idx]),
                            contentDescription = null,
                            tint = tint
                        )
                    },
                    headlineContent = { Text(acc.name) },
                    supportingContent = {
                        Text(stringResource(R.string.accounts_item_meta, acc.type.localized(), acc.currency))
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAccountClick(acc.id) }
                )
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
    onIconIndex: (Int) -> Unit,
    onIconColor: (Long?) -> Unit,
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

            var typeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                OutlinedTextField(
                    value = draft.type.localized(),
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
                            text = { Text(t.localized()) },
                            onClick = { onType(t); typeExpanded = false }
                        )
                    }
                }
            }

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

            val digits = remember(draft.currency) {
                runCatching { Currency.getInstance(draft.currency).defaultFractionDigits }
                    .getOrDefault(2).coerceAtLeast(0)
            }

            OutlinedTextField(
                value = draft.balanceText,
                onValueChange = onBalanceText,
                label = { Text(stringResource(R.string.accounts_initial_balance_label, draft.currency)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (digits == 0) KeyboardType.Number else KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(stringResource(R.string.accounts_icon_label), style = MaterialTheme.typography.labelLarge)
            IconGrid(
                selected = draft.iconIndex,
                onSelect = onIconIndex
            )

            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.accounts_color_label), style = MaterialTheme.typography.labelLarge)
            ColorChooser(
                selected = draft.iconColorArgb,
                onChange = onIconColor
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

@Composable
private fun CurrencyTotalsCard(totals: List<CurrencyTotalUi>, modifier: Modifier = Modifier) {
    if (totals.isEmpty()) return
    ElevatedCard(modifier.fillMaxWidth().padding(12.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.accounts_totals_title), style = MaterialTheme.typography.titleMedium)
            totals.forEach { t ->
                val formatted = remember(t.totalMinor, t.currency) {
                    NumberUtils.formatAmountPlain(t.totalMinor, t.currency, trimZeroDecimals = true)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(t.currency, style = MaterialTheme.typography.bodyMedium)
                    Text(formatted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun IconGrid(selected: Int, onSelect: (Int) -> Unit) {
    val icons = listOf(
        CommonDrawables.account_icon_1, CommonDrawables.account_icon_2, CommonDrawables.account_icon_3, CommonDrawables.account_icon_4,
        CommonDrawables.account_icon_5, CommonDrawables.account_icon_6, CommonDrawables.account_icon_7, CommonDrawables.account_icon_8
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (col in 0 until 4) {
                    val idx = row * 4 + col
                    val number = idx + 1
                    val isSel = number == selected
                    ElevatedCard(
                        onClick = { onSelect(number) },
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(icons[idx]),
                                contentDescription = null,
                                tint = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}