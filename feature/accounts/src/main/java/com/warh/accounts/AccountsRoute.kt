package com.warh.accounts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warh.commons.NumberUtils
import com.warh.commons.NumberUtils.formatHeroAmount
import com.warh.commons.TopBarDefault
import com.warh.commons.bottom_bar.FabSpec
import com.warh.commons.bottom_bar.LocalBottomBarBehavior
import com.warh.commons.charts.DonutChart
import com.warh.commons.charts.DonutSlice
import com.warh.designsystem.ExpenseTheme
import com.warh.domain.models.Account
import com.warh.domain.models.AccountType
import org.koin.androidx.compose.koinViewModel
import com.warh.commons.R.drawable as CommonDrawables

@Composable
fun AccountsRoute(
    vm: AccountsViewModel = koinViewModel(),
    setFab: (FabSpec?) -> Unit,
    onAccountClick: (Long) -> Unit,
    onNavigateToAdd: (Long?) -> Unit,
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    SideEffect {
        setFab(
            FabSpec(
                visible = !ui.loading && ui.accounts.isNotEmpty(),
                onClick = { onNavigateToAdd(null) }
            ) { Icon(Icons.Default.Add, null) }
        )
    }

    AccountsScreen(
        ui = ui,
        onAccountClick = onAccountClick,
        onNavigateToAdd = onNavigateToAdd,
        onDelete = vm::delete
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountsScreen(
    ui: AccountsUiState,
    onAccountClick: (Long) -> Unit,
    onNavigateToAdd: (Long?) -> Unit,
    onDelete: (Long) -> Unit,
) {
    val layoutDir = LocalLayoutDirection.current
    val bottomSb = LocalBottomBarBehavior.current
    val appBarState = rememberTopAppBarState()
    val topSb  = TopAppBarDefaults.enterAlwaysScrollBehavior(appBarState)

    var openMenuId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }

    val scrollMods = Modifier
        .nestedScroll(topSb.nestedScrollConnection)
        .then(bottomSb?.let { Modifier.nestedScroll(it.connection) } ?: Modifier)

    val iconIds = rememberSaveable {
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
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = inner.calculateStartPadding(layoutDir),
                    top = inner.calculateTopPadding(),
                    end = inner.calculateEndPadding(layoutDir),
                    bottom = 0.dp
                )
                .then(scrollMods)
        ) {
            when {
                ui.loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                !ui.loading && ui.accounts.isEmpty() -> {
                    AccountsEmptyState(
                        modifier = Modifier.fillMaxSize(),
                        onPrimaryAction = { onNavigateToAdd(null) }
                    )
                }
                else -> {
                    if (openMenuId != null) {
                        Box(
                            Modifier
                                .matchParentSize()
                                .zIndex(0f)
                                .background(Color.Transparent)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { openMenuId = null }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = inner.calculateBottomPadding() + 24.dp)
                    ) {
                        item {
                            AccountsRingByCurrency(
                                accounts = ui.accounts,
                                totals   = ui.totalsByCurrency
                            )
                        }

                        items(ui.accounts, key = { it.id!! }) { acc ->
                            val cs = MaterialTheme.colorScheme
                            val idx = (acc.iconIndex - 1).coerceIn(0, iconIds.lastIndex)
                            val tint = acc.iconColorArgb?.let { Color(it.toInt()) } ?: cs.onSurfaceVariant

                            val balanceText = NumberUtils.formatAmountPlain(
                                minor = acc.balance,
                                code = acc.currency,
                                trimZeroDecimals = true
                            )

                            val balanceColor = if (acc.balance > 0L) Color(0xFF2E7D32) else cs.error

                            AccountRowCard(
                                name = acc.name,
                                subtitle = acc.type.localized(),
                                iconPainter = painterResource(iconIds[idx]),
                                iconTint = tint,
                                balanceText = balanceText,
                                balanceColor = balanceColor,
                                currency = acc.currency,
                                isMenuOpen = openMenuId == acc.id,
                                onLongPress = { openMenuId = acc.id },
                                onCardTap = {
                                    if (openMenuId == null) acc.id?.let(onAccountClick)
                                    else openMenuId = null
                                },
                                onEdit = {
                                    acc.id?.let(onNavigateToAdd)
                                    openMenuId = null
                                },
                                onDeleteRequest = {
                                    acc.id?.let { pendingDeleteId = it }
                                    openMenuId = null
                                }
                            )
                        }
                    }
                }
            }

            val toDelete = pendingDeleteId
            if (toDelete != null) {
                DeleteAccountDialog(
                    onDismiss = { pendingDeleteId = null },
                    onConfirm = {
                        onDelete(toDelete)
                        pendingDeleteId = null
                    }
                )
            }
        }
    }
}

@Composable
private fun AccountRowCard(
    name: String,
    subtitle: String,
    iconPainter: Painter,
    iconTint: Color,
    balanceText: String,
    balanceColor: Color,
    currency: String,
    isMenuOpen: Boolean,
    onLongPress: () -> Unit,
    onCardTap: () -> Unit,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        ElevatedCard(
            shape = shape,
            colors = CardDefaults.elevatedCardColors(containerColor = cs.secondaryContainer),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onCardTap,
                    onLongClick = onLongPress
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Icon(painter = iconPainter, contentDescription = null, tint = iconTint)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = cs.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = balanceText, style = MaterialTheme.typography.titleMedium, color = balanceColor)
                    Text(
                        text = currency,
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .zIndex(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedVisibility(
                visible = isMenuOpen,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SmallRoundAction(
                        icon = Icons.Default.Edit,
                        tint = cs.onPrimaryContainer,
                        container = cs.primaryContainer,
                        onClick = onEdit
                    )
                    SmallRoundAction(
                        icon = Icons.Default.Delete,
                        tint = cs.onPrimaryContainer,
                        container = cs.primaryContainer,
                        onClick = onDeleteRequest
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallRoundAction(
    icon: ImageVector,
    tint: Color,
    container: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = container,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = tint)
        }
    }
}

@Composable
private fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                Modifier
                    .size(44.dp)
                    .background(cs.errorContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, null, tint = cs.error)
            }
        },
        title = { Text(stringResource(R.string.accounts_delete_account_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.accounts_delete_account_body),
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.accounts_delete_account_irreversible),
                    color = cs.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = cs.error)
            ) { Text(stringResource(R.string.accounts_delete_action_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.accounts_delete_action_cancel)) }
        },
        containerColor = cs.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun AccountsEmptyState(
    modifier: Modifier = Modifier,
    onPrimaryAction: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp),
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

    val showSelector = currencyChips.size > 1
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
        formatHeroAmount(
            minor = totalForSelectedMinor,
            currency = selectedCode
        )
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
        if (showSelector) {
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

@Preview(name = "Accounts — List (Light)", showBackground = true)
@Composable
fun AccountsScreenPreview_List_Light() {
    ExpenseTheme(dark = false) {
        AccountsScreen(
            ui = uiListState(),
            onAccountClick = {},
            onNavigateToAdd = {},
            onDelete = {}
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
            onNavigateToAdd = {},
            onDelete = {}
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
    totalsByCurrency = listOf(
        CurrencyTotalUi("ARS", 77_500),
        CurrencyTotalUi("USD", 125_000)
    )
)