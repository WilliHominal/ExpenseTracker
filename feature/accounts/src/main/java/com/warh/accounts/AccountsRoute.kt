package com.warh.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warh.accounts.utils.BalanceUtils.formatMajor
import com.warh.commons.NumberUtils
import com.warh.commons.TopBarDefault
import com.warh.commons.bottom_bar.FabSpec
import com.warh.commons.bottom_bar.LocalBottomBarBehavior
import com.warh.commons.charts.DonutChart
import com.warh.commons.charts.DonutSlice
import com.warh.commons.color_picker.ColorChooser
import com.warh.designsystem.ExpenseTheme
import com.warh.domain.models.Account
import com.warh.domain.models.AccountType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Currency
import com.warh.commons.R.drawable as CommonDrawables

@Composable
fun AccountsRoute(
    vm: AccountsViewModel = koinViewModel(),
    setFab: (FabSpec?) -> Unit,
    onAccountClick: (Long) -> Unit,
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    SideEffect {
        setFab(FabSpec(visible = ui.draft == null, onClick = vm::startAdd) {
            Icon(Icons.Default.Add, null)
        })
    }

    AccountsScreen(
        ui = ui,
        onStartEdit = vm::startEdit,
        onDelete = { acc, show -> vm.delete(acc, onBlocked = show) },
        onAccountClick = onAccountClick,
        onName = vm::onName,
        onType = vm::onType,
        onCurrency = vm::onCurrency,
        onBalanceText = vm::onBalanceText,
        onIconIndex = vm::onIconIndex,
        onIconColor = vm::onIconColor,
        onCancel = vm::cancelEdit,
        onSave = { show -> vm.saveEdit(onError = show) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountsScreen(
    ui: AccountsUiState,
    onStartEdit: (Account) -> Unit,
    onDelete: (Account, (String) -> Unit) -> Unit,
    onAccountClick: (Long) -> Unit,
    onName: (String) -> Unit,
    onType: (AccountType) -> Unit,
    onCurrency: (String) -> Unit,
    onBalanceText: (String) -> Unit,
    onIconIndex: (Int) -> Unit,
    onIconColor: (Long?) -> Unit,
    onCancel: () -> Unit,
    onSave: ((String) -> Unit) -> Unit
) {
    val layoutDir = LocalLayoutDirection.current
    val bottomSb = LocalBottomBarBehavior.current
    val appBarState = rememberTopAppBarState()
    val topSb  = TopAppBarDefaults.enterAlwaysScrollBehavior(appBarState)

    val snackBar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val show: (String) -> Unit = { msg -> scope.launch { snackBar.showSnackbar(msg) } }

    val hasDraft = ui.draft != null
    LaunchedEffect(hasDraft) {
        if (!hasDraft) bottomSb?.reset()
    }

    val iconIds = remember {
        listOf(
            CommonDrawables.account_icon_1, CommonDrawables.account_icon_2,
            CommonDrawables.account_icon_3, CommonDrawables.account_icon_4,
            CommonDrawables.account_icon_5, CommonDrawables.account_icon_6,
            CommonDrawables.account_icon_7, CommonDrawables.account_icon_8
        )
    }

    Scaffold(
        topBar = {
            TopBarDefault(
                title = stringResource(R.string.accounts_title),
                scrollBehavior = topSb
            )
        },
        snackbarHost = { SnackbarHost(snackBar) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start  = inner.calculateStartPadding(layoutDir),
                    top    = inner.calculateTopPadding(),
                    end    = inner.calculateEndPadding(layoutDir),
                    bottom = 0.dp,
                )
                .nestedScroll(topSb.nestedScrollConnection)
                .then(bottomSb?.let { Modifier.nestedScroll(it.connection) } ?: Modifier),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AccountsRingByCurrency(
                    accounts = ui.accounts,
                    totals   = ui.totalsByCurrency,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }

            ui.draft?.let { d ->
                item {
                    AccountEditorCard(
                        draft = d,
                        onName = onName,
                        onType = onType,
                        onCurrency = onCurrency,
                        onBalanceText = onBalanceText,
                        onIconIndex = onIconIndex,
                        onIconColor = onIconColor,
                        onCancel = onCancel,
                        onSave = { onSave(show) }
                    )
                }
            }

            items(ui.accounts, key = { it.id!! }) { acc ->
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
                        Text(stringResource(R.string.accounts_item_meta, acc.type.localized(), formatMajor(acc.balance, acc.currency), acc.currency))
                    },
                    trailingContent = {
                        Row {
                            TextButton(onClick = { onStartEdit(acc) }) { Text(stringResource(R.string.accounts_edit)) }
                            IconButton(onClick = { onDelete(acc, show) }) {
                                Icon(Icons.Default.Delete, null)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { acc.id?.let { onAccountClick(it) } }
                )
            }
        }
    }
}

@Composable
private fun AccountsRingByCurrency(
    accounts: List<Account>,
    totals: List<CurrencyTotalUi>,
    modifier: Modifier = Modifier
) {
    if (totals.isEmpty()) return

    val cs = MaterialTheme.colorScheme
    var selectedCode by remember(totals) { mutableStateOf(totals.first().currency) }
    val totalForSelected = totals.firstOrNull { it.currency == selectedCode } ?: return

    data class Slice(val name: String, val value: Long, val ratio: Float)
    val slicesData = remember(accounts, selectedCode) {
        val items = accounts.filter { it.currency == selectedCode }
            .map { it.name to kotlin.math.abs(it.balance) }
            .filter { it.second > 0L }
        val sum = items.sumOf { it.second }.takeIf { it > 0 } ?: 1L
        items.map { (n, v) -> Slice(n, v, v.toFloat() / sum.toFloat()) }
            .sortedByDescending { it.value }
    }
    if (slicesData.isEmpty()) return

    val formattedTotal = remember(totalForSelected.totalMinor, selectedCode) {
        NumberUtils.formatAmountPlain(totalForSelected.totalMinor, selectedCode, trimZeroDecimals = true)
    }

    var selectedSlice by remember { mutableStateOf<Int?>(null) }
    var lastTapAngle by remember { mutableStateOf<Float?>(null) }

    val donutSlices = remember(slicesData) {
        slicesData.map { DonutSlice(it.name, it.ratio) }
    }

    val nameToColor = remember(accounts, selectedCode) {
        accounts
            .filter { it.currency == selectedCode && kotlin.math.abs(it.balance) > 0 }
            .associate { acc ->
                acc.name to (acc.iconColorArgb?.let { Color(it.toInt()) })
            }
    }

    val colorForAccount: (String) -> Color = remember(nameToColor) {
        { key -> nameToColor[key] ?: cs.onSurfaceVariant }
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            totals.forEach { t ->
                FilterChip(
                    selected = t.currency == selectedCode,
                    onClick = { selectedCode = t.currency; selectedSlice = null; lastTapAngle = null },
                    label = { Text(t.currency) }
                )
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            DonutChart(
                slices = donutSlices,
                colorFor = colorForAccount,
                modifier = Modifier.fillMaxSize(),
                selectedIndex = selectedSlice,
                onSelectedIndexChange = { selectedSlice = it },
                lastTapAngle = lastTapAngle,
                onLastTapAngleChange = { lastTapAngle = it }
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.accounts_totals_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(text = formattedTotal, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = selectedCode,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

@Preview(name = "Accounts — List (Light)", showBackground = true)
@Composable
fun AccountsScreenPreview_List_Light() {
    ExpenseTheme(dark = false) {
        AccountsScreen(
            ui = uiListState(),
            onStartEdit = {},
            onDelete = { _, _ -> },
            onAccountClick = {},
            onName = {},
            onType = {},
            onCurrency = {},
            onBalanceText = {},
            onIconIndex = {},
            onIconColor = {},
            onCancel = {},
            onSave = {}
        )
    }
}

@Preview(name = "Accounts — List (Dark)", showBackground = true)
@Composable
fun AccountsScreenPreview_List_Dark() {
    ExpenseTheme(dark = true) {
        AccountsScreen(
            ui = uiListState(),
            onStartEdit = {},
            onDelete = { _, _ -> },
            onAccountClick = {},
            onName = {},
            onType = {},
            onCurrency = {},
            onBalanceText = {},
            onIconIndex = {},
            onIconColor = {},
            onCancel = {},
            onSave = {}
        )
    }
}

@Preview(name = "Accounts — Draft (Light)", showBackground = true)
@Composable
fun AccountsScreenPreview_Draft_Light() {
    ExpenseTheme(dark = false) {
        AccountsScreen(
            ui = uiDraftState(),
            onStartEdit = {},
            onDelete = { _, _ -> },
            onAccountClick = {},
            onName = {},
            onType = {},
            onCurrency = {},
            onBalanceText = {},
            onIconIndex = {},
            onIconColor = {},
            onCancel = {},
            onSave = {}
        )
    }
}

@Preview(name = "Accounts — Draft (Dark)", showBackground = true)
@Composable
fun AccountsScreenPreview_Draft_Dark() {
    ExpenseTheme(dark = true) {
        AccountsScreen(
            ui = uiDraftState(),
            onStartEdit = {},
            onDelete = { _, _ -> },
            onAccountClick = {},
            onName = {},
            onType = {},
            onCurrency = {},
            onBalanceText = {},
            onIconIndex = {},
            onIconColor = {},
            onCancel = {},
            onSave = {}
        )
    }
}

private fun sampleAccounts(): List<Account> = listOf(
    Account(1, "Efectivo", AccountType.CASH, "ARS", 152_500, 152_500, 1, null),
    Account(2, "Banco", AccountType.BANK, "USD", 1_250_00, 1_250_00, 2, 0xFF64B5F6),
    Account(3, "Tarjeta", AccountType.WALLET, "ARS", -75_000, -75_000, 3, 0xFFE57373),
)

private fun uiListState() = AccountsUiState(
    accounts = sampleAccounts(),
    draft = null,
    totalsByCurrency = listOf(
        CurrencyTotalUi("ARS", 77_500),
        CurrencyTotalUi("USD", 125_000)
    )
)

private fun uiDraftState() = AccountsUiState(
    accounts = sampleAccounts(),
    draft = AccountDraft(
        id = null,
        name = "Nueva cuenta",
        type = AccountType.BANK,
        currency = "ARS",
        balanceText = "1000",
        iconIndex = 2,
        iconColorArgb = null
    ),
    totalsByCurrency = uiListState().totalsByCurrency
)
