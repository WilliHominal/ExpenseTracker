package com.warh.accounts

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import androidx.compose.ui.layout.ContentScale
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
        setFab(FabSpec(visible = ui.draft == null && ui.accounts.isNotEmpty(), onClick = vm::startAdd) {
            Icon(Icons.Default.Add, null)
        })
    }

    AccountsScreen(
        ui = ui,
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
        val showEmpty = ui.accounts.isEmpty() && ui.draft == null

        if (showEmpty) {
            AccountsEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start  = inner.calculateStartPadding(layoutDir),
                        top    = inner.calculateTopPadding(),
                        end    = inner.calculateEndPadding(layoutDir),
                        bottom = 0.dp
                    ),
                onPrimaryAction = { /* TODO: Navegar a ADD Account */ }
            )
        } else {
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
                        totals   = ui.totalsByCurrency
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
                    val cs = MaterialTheme.colorScheme
                    val idx = (acc.iconIndex - 1).coerceIn(0, iconIds.lastIndex)
                    val tint = acc.iconColorArgb?.let { Color(it.toInt()) } ?: cs.onSurfaceVariant
                    val balanceText = formatMajor(acc.balance, acc.currency)
                    val balanceColor = if (acc.balance > 0L) Color(0xFF2E7D32) else cs.error

                    ElevatedCard(
                        onClick = { acc.id?.let(onAccountClick) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = cs.secondaryContainer
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Icon(
                                painter = painterResource(iconIds[idx]),
                                contentDescription = null,
                                tint = tint
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = acc.type.localized(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = cs.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = balanceText,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = balanceColor
                                )
                                Text(
                                    text = acc.currency,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = cs.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountsEmptyState(
    modifier: Modifier = Modifier,
    onPrimaryAction: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(horizontal = 24.dp).padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = cs.surfaceContainerLowest
            ),
            elevation = CardDefaults.elevatedCardElevation(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(CommonDrawables.accounts_empty_img),
                    contentDescription = null,
                    modifier = Modifier.size(132.dp),
                    contentScale = ContentScale.FillBounds
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.accounts_empty_title),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.accounts_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = cs.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        Button(onClick = onPrimaryAction) {
            Text(stringResource(R.string.accounts_empty_cta))
        }
    }
}

@Composable
private fun AccountsRingByCurrency(
    accounts: List<Account>,
    totals: List<CurrencyTotalUi>,
) {
    val currencyChips = remember(totals, accounts) {
        val fromTotals = totals.map { it.currency }
        fromTotals.ifEmpty { accounts.map { it.currency }.distinct().sorted() }
    }
    if (currencyChips.isEmpty()) return

    val cs = MaterialTheme.colorScheme

    var selectedCode by remember(currencyChips) { mutableStateOf(currencyChips.first()) }
    LaunchedEffect(currencyChips) {
        if (selectedCode !in currencyChips) selectedCode = currencyChips.first()
    }

    val totalForSelectedMinor = remember(totals, selectedCode) {
        totals.firstOrNull { it.currency == selectedCode }?.totalMinor ?: 0L
    }

    data class Slice(val name: String, val value: Long, val ratio: Float)

    val slicesData = remember(accounts, selectedCode) {
        val items = accounts
            .filter { it.currency == selectedCode }
            .map { it.name to kotlin.math.abs(it.balance) }

        when {
            items.isEmpty() -> listOf(Slice("—", 0L, 1f))
            else -> {
                val sum = items.sumOf { it.second }
                if (sum == 0L) {
                    val n = items.size
                    val equal = 1f / n
                    items.mapIndexed { i, (nme, v) ->
                        val ratio = if (i == n - 1) 1f - equal * (n - 1) else equal
                        Slice(nme, v, ratio)
                    }
                } else {
                    items.map { (n, v) -> Slice(n, v, v.toFloat() / sum.toFloat()) }
                        .sortedByDescending { it.value }
                }
            }
        }
    }

    val formattedTotal = remember(totalForSelectedMinor, selectedCode) {
        formatMajor(totalForSelectedMinor, selectedCode)
    }

    var selectedSlice by remember { mutableStateOf<Int?>(null) }
    var lastTapAngle by remember { mutableStateOf<Float?>(null) }

    val donutSlices = remember(slicesData) {
        slicesData.map { DonutSlice(it.name, it.ratio) }
    }

    val nameToColor = remember(accounts, selectedCode) {
        accounts
            .filter { it.currency == selectedCode }
            .associate { acc ->
                acc.name to (acc.iconColorArgb?.let { Color(it.toInt()) })
            }
    }

    val colorForAccount: (String) -> Color = remember(nameToColor) {
        { key -> nameToColor[key] ?: cs.onSurfaceVariant }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        if (currencyChips.count() > 4) {
            CurrencyTabs(currencyChips, selectedCode) { code ->
                selectedCode = code; selectedSlice = null; lastTapAngle = null
            }
        } else {
            CurrencySegmented(
                options = currencyChips,
                selected = selectedCode,
                onSelect = { x ->
                    selectedCode = x
                    selectedSlice = null
                    lastTapAngle = null
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
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

@Composable
private fun CurrencySegmented(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier) {
        options.forEachIndexed { i, code ->
            SegmentedButton(
                selected = code == selected,
                onClick = { onSelect(code) },
                shape = SegmentedButtonDefaults.itemShape(i, options.size),
                label = { Text(code) }
            )
        }
    }
}

@Composable
private fun CurrencyTabs(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
    ScrollableTabRow(selectedTabIndex = selectedIndex, edgePadding = 16.dp) {
        options.forEachIndexed { i, code ->
            Tab(
                selected = i == selectedIndex,
                onClick = { onSelect(code) },
                text = { Text(code) }
            )
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

@Preview(name = "Accounts — Empty (Light)", showBackground = true)
@Composable
fun AccountsScreenPreview_Empty_Light() {
    ExpenseTheme(dark = false) {
        AccountsEmptyState(onPrimaryAction = {})
    }
}

@Preview(name = "Accounts — Empty (Dark)", showBackground = true)
@Composable
fun AccountsScreenPreview_Empty_Dark() {
    ExpenseTheme(dark = true) {
        AccountsEmptyState(onPrimaryAction = {})
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