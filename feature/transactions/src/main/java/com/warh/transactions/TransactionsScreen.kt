package com.warh.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.warh.commons.DateUtils.formatDateTime
import com.warh.commons.NumberUtils.formatAmountWithCode
import com.warh.commons.TopBarDefault
import com.warh.commons.bottom_bar.FabSpec
import com.warh.designsystem.ExpenseTheme
import com.warh.domain.models.Account
import com.warh.domain.models.AccountType
import com.warh.domain.models.Category
import com.warh.domain.models.Transaction
import com.warh.domain.models.TransactionFilter
import com.warh.domain.models.TxType
import kotlinx.coroutines.flow.flowOf
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import com.warh.commons.R.drawable as CommonDrawables

//TODO: Transacciones recurrentes o programadas, en cantidad o porcentaje (tipo plazo fijo)

@Composable
fun TransactionsRoute(
    vm: TransactionsViewModel = koinViewModel(),
    vmExport: TransactionsExportViewModel = koinViewModel(),
    setFab: (FabSpec?) -> Unit,
    onNavigateToAdd: (Long?) -> Unit,
) {
    val pagingItems = vm.paging.collectAsLazyPagingItems()
    val filter by vm.filter.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val filtersVisible by vm.filtersVisible.collectAsStateWithLifecycle()
    val openMenuId by vm.openMenuId.collectAsStateWithLifecycle()
    val pendingDeleteId by vm.pendingDeleteId.collectAsStateWithLifecycle()
    val expandedId by vm.expandedId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val isLoading = pagingItems.loadState.refresh is LoadState.Loading
    val hasItems  = pagingItems.itemCount > 0

    SideEffect {
        setFab(
            FabSpec(visible = !isLoading && hasItems, onClick = { onNavigateToAdd(null) }) {
                Icon(Icons.Default.Add, null)
            }
        )
    }

    TransactionsScreen(
        filter = filter,
        accounts = accounts,
        categories = categories,
        pagingItems = pagingItems,
        filtersVisible = filtersVisible,
        onToggleFilters = vm::toggleFiltersVisible,
        onQueryChange = vm::setText,
        onDateRangeChange = vm::setDateRange,
        onToggleAccount = vm::toggleAccount,
        onToggleCategory = vm::toggleCategory,
        onExportClick = { vmExport.exportCsv(context, filter) },
        onNavigateToAdd = onNavigateToAdd,
        openMenuId = openMenuId,
        pendingDeleteId = pendingDeleteId,
        expandedId = expandedId,
        onOpenMenu = vm::openMenu,
        onCloseMenu = vm::closeMenu,
        onRequestDelete = vm::requestDelete,
        onCancelDelete = vm::cancelDelete,
        onConfirmDelete = vm::confirmDelete,
        onToggleItem = vm::toggleExpanded,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    filter: TransactionFilter,
    accounts: List<Account>,
    categories: List<Category>,
    pagingItems: LazyPagingItems<Transaction>,
    filtersVisible: Boolean,
    onToggleFilters: () -> Unit,
    onQueryChange: (String) -> Unit,
    onDateRangeChange: (LocalDateTime?, LocalDateTime?) -> Unit,
    onToggleAccount: (Long) -> Unit,
    onToggleCategory: (Long) -> Unit,
    onExportClick: () -> Unit,
    onNavigateToAdd: (Long?) -> Unit,
    openMenuId: Long?,
    pendingDeleteId: Long?,
    expandedId: Long?,
    onOpenMenu: (Long) -> Unit,
    onCloseMenu: () -> Unit,
    onRequestDelete: (Long) -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onToggleItem: (Long) -> Unit,
) {
    val appBarState = rememberTopAppBarState()
    val topSb  = TopAppBarDefaults.enterAlwaysScrollBehavior(appBarState)

    val isLoading = pagingItems.loadState.refresh is LoadState.Loading
    val isEmpty   = !isLoading && pagingItems.itemCount == 0

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopBarDefault(
                title = stringResource(R.string.transactions_title),
                actions = {
                    IconButton(onClick = onExportClick) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = stringResource(R.string.transactions_export_cd)
                        )
                    }
                    IconButton(onClick = onToggleFilters) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.transactions_filter_cd),
                        )
                    }
                },
                scrollBehavior = topSb
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedVisibility(
                visible = filtersVisible,
                enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(150)),
                exit = ExitTransition.None
            ) {
                FilterBar(
                    filter = filter,
                    accounts = accounts,
                    categories = categories,
                    onQueryChange = onQueryChange,
                    onDateRangeChange = onDateRangeChange,
                    onToggleAccount = onToggleAccount,
                    onToggleCategory = onToggleCategory,
                )
            }

            when {
                isLoading -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .nestedScroll(topSb.nestedScrollConnection),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                    }
                }

                isEmpty -> {
                    TransactionsEmptyState(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(topSb.nestedScrollConnection),
                        onPrimaryAction = { onNavigateToAdd(null) }
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(topSb.nestedScrollConnection)
                    ) {
                        // Overlay para cerrar el menú si hay uno abierto
                        if (openMenuId != null) {
                            Box(
                                Modifier
                                    .matchParentSize()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { onCloseMenu() }
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(
                                count = pagingItems.itemCount,
                                key = { idx -> pagingItems.peek(idx)?.id ?: idx }
                            ) { idx ->
                                val tx = pagingItems[idx] ?: return@items

                                val account = accounts.firstOrNull { it.id == tx.accountId }
                                val currencyCode = account?.currency ?: Currency.getInstance(Locale.getDefault()).currencyCode
                                val category = categories.firstOrNull { it.id == tx.categoryId }

                                val currDay = tx.date.toLocalDate()
                                val prevDay = if (idx > 0) {
                                    pagingItems.peek(idx - 1)?.date?.toLocalDate()
                                } else null

                                val shouldShowHeader = (idx == 0) || (prevDay == null) || (currDay != prevDay)

                                if (shouldShowHeader) {
                                    DayHeaderRow(day = currDay)
                                    Spacer(Modifier.height(4.dp))
                                }

                                TransactionRowCard(
                                    tx = tx,
                                    currencyCode = currencyCode,
                                    categoryName = category?.name,
                                    categoryColor = category?.iconColorArgb?.let { Color(it.toInt()) },
                                    accountName = account?.name,
                                    isMenuOpen = openMenuId == tx.id,
                                    isExpanded = expandedId == tx.id,
                                    onLongPress = { onOpenMenu(tx.id) },
                                    onCardTap = {
                                        if (openMenuId == null) onToggleItem(tx.id) else onCloseMenu()
                                    },
                                    onEdit = {
                                        onNavigateToAdd(tx.id)
                                        onCloseMenu()
                                    },
                                    onDeleteRequest = {
                                        onRequestDelete(tx.id)
                                        onCloseMenu()
                                    }
                                )

                                HorizontalDivider(thickness = 0.5.dp)
                            }
                        }

                        // Diálogo de confirmación de borrado
                        if (pendingDeleteId != null) {
                            DeleteTransactionDialog(
                                onDismiss = onCancelDelete,
                                onConfirm = onConfirmDelete
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeaderRow(day: java.time.LocalDate) {
    val cs = MaterialTheme.colorScheme
    val today = java.time.LocalDate.now()
    val rawLabel = when (day) {
        today -> stringResource(R.string.transactions_today)
        today.minusDays(1) -> stringResource(R.string.transactions_yesterday)
        else -> day.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM"))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 0.5.dp,
            color = cs.outlineVariant
        )

        Text(
            text = rawLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                .uppercase(), // look más “sección”
            style = MaterialTheme.typography.labelMedium,
            color = cs.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
            maxLines = 1
        )

        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 0.5.dp,
            color = cs.outlineVariant
        )
    }
}

@Composable
private fun TransactionRowCard(
    tx: Transaction,
    currencyCode: String,
    categoryName: String?,
    categoryColor: Color?,
    accountName: String?,
    isMenuOpen: Boolean,
    isExpanded: Boolean,
    onLongPress: () -> Unit,
    onCardTap: () -> Unit,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val cs = MaterialTheme.colorScheme

    val amountText = remember(tx.amountMinor, currencyCode) {
        formatAmountWithCode(tx.amountMinor, currencyCode)
    }

    val timeText = remember(tx.date) {
        DateTimeFormatter.ofPattern("HH:mm").format(tx.date)
    }

    val amountColor = when (tx.type) {
        TxType.INCOME  -> Color(0xFF2E7D32)
        TxType.EXPENSE -> cs.error
        else           -> cs.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onCardTap,
                    onLongClick = onLongPress
                )
                .padding(vertical = 10.dp)
                .animateContentSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(36.dp)
                        .background(categoryColor ?: cs.outlineVariant, RoundedCornerShape(2.dp))
                )

                Spacer(Modifier.width(12.dp))

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tx.merchant ?: tx.note
                        ?: stringResource(R.string.transactions_transaction_default_name),
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(Modifier.width(8.dp))

                    TimePill(timeText = timeText)
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium,
                    color = amountColor
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(160)) + fadeIn(),
                exit  = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!tx.note.isNullOrBlank() && tx.note != tx.merchant) {
                        Text(tx.note!!, style = MaterialTheme.typography.bodyMedium)
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!accountName.isNullOrBlank()) SmallInfoChip(label = accountName)
                        if (categoryName != null) {
                            SmallInfoChip(
                                label = categoryName,
                                dotColor = categoryColor ?: cs.outlineVariant
                            )
                        }
                        SmallInfoChip(
                            label = stringResource(
                                if (tx.type == TxType.EXPENSE)
                                    R.string.add_transaction_tx_type_expense
                                else
                                    R.string.add_transaction_tx_type_income
                            )
                        )
                    }
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
            androidx.compose.animation.AnimatedVisibility(
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
private fun TimePill(timeText: String) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = cs.surfaceContainerHighest,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = timeText,
            style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun SmallInfoChip(
    label: String,
    dotColor: Color? = null
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cs.surfaceContainerLowest,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dotColor != null) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(dotColor, CircleShape)
                )
                Spacer(Modifier.size(6.dp))
            }
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = cs.onSurface)
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
private fun DeleteTransactionDialog(
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
        title = { Text(stringResource(R.string.transactions_delete_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.transactions_delete_body),
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.transactions_delete_irreversible),
                    color = cs.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = cs.error)
            ) { Text(stringResource(R.string.transactions_delete_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.transactions_delete_cancel)) }
        },
        containerColor = cs.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun TransactionsEmptyState(
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
                    painter = painterResource(CommonDrawables.transactions_empty_img),
                    contentDescription = null,
                    modifier = Modifier.size(132.dp),
                    contentScale = ContentScale.FillBounds
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.transactions_empty_title),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.transactions_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = cs.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        Button(onClick = onPrimaryAction) {
            Text(stringResource(R.string.transactions_empty_cta))
        }
    }
}

@Composable
private fun FilterBar(
    filter: TransactionFilter,
    accounts: List<Account>,
    categories: List<Category>,
    onQueryChange: (String) -> Unit,
    onDateRangeChange: (LocalDateTime?, LocalDateTime?) -> Unit,
    onToggleAccount: (Long) -> Unit,
    onToggleCategory: (Long) -> Unit,
) {
    Column(Modifier
        .fillMaxWidth()
        .padding(12.dp)) {
        OutlinedTextField(
            value = filter.text ?: "",
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.transactions_search_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = {
                val ym = YearMonth.now()
                onDateRangeChange(ym.atDay(1).atStartOfDay(), ym.plusMonths(1).atDay(1).atStartOfDay())
            }) { Text(stringResource(R.string.transactions_this_month)) }
            TextButton(onClick = { onDateRangeChange(null, LocalDateTime.now()) }) {
                Text(stringResource(R.string.transactions_all_time))
            }
        }

        Spacer(Modifier.height(8.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            accounts.forEach { acc ->
                val selected = acc.id in filter.accountIds
                FilterChip(
                    selected = selected,
                    onClick = { acc.id?.let { onToggleAccount(it) } },
                    label = { Text(acc.name) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.forEach { cat ->
                val selected = cat.id in filter.categoryIds
                FilterChip(
                    selected = selected,
                    onClick = { onToggleCategory(cat.id) },
                    label = { Text(cat.name) }
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(
    tx: Transaction,
    currencyCode: String
) {
    val amountText = remember(tx.amountMinor, currencyCode) {
        formatAmountWithCode(tx.amountMinor, currencyCode)
    }
    val dateText = remember(tx.date) {
        formatDateTime(tx.date)
    }

    ListItem(
        headlineContent = {
            Text(tx.merchant ?: tx.note ?: stringResource(R.string.transactions_transaction_default_name))
        },
        supportingContent = {
            Text(stringResource(R.string.transactions_row_support, amountText, dateText))
        }
    )
    HorizontalDivider(thickness = 0.5.dp)
}

@Preview(name = "Transactions – Dark", showBackground = true)
@Composable
fun TransactionsScreenPreviewDark() {
    ExpenseTheme(dark = true) {
        val sampleAccounts = listOf(
            Account(1, "Banco", AccountType.BANK, "ARS", 0),
            Account(2, "Efectivo", AccountType.CASH, "ARS", 0)
        )
        val sampleCategories = listOf(
            Category(1, "Comida", 1, 0xFFE57373, TxType.EXPENSE),
            Category(2, "Transporte", 2, 0xFF64B5F6, TxType.EXPENSE),
            Category(3, "Hogar", 3, 0xFF81C784, TxType.INCOME)
        )
        val sampleTx = listOf(
            Transaction(
                id = 1L, accountId = 1L, type = TxType.EXPENSE,
                amountMinor = 123000, date = LocalDateTime.now(),
                categoryId = 1L, merchant = "Café", note = null
            ),
            Transaction(
                id = 2L, accountId = 2L, type = TxType.EXPENSE,
                amountMinor = 12500, date = LocalDateTime.now(),
                categoryId = 2L, merchant = null, note = "si"
            )
        )

        val pagingItems = remember(sampleTx) {
            flowOf(PagingData.from(sampleTx))
        }.collectAsLazyPagingItems()

        TransactionsScreen(
            filter = TransactionFilter(),
            accounts = sampleAccounts,
            categories = sampleCategories,
            pagingItems = pagingItems,
            expandedId = null,
            filtersVisible = true,
            onToggleFilters = {},
            onQueryChange = {},
            onDateRangeChange = { _, _ -> },
            onToggleAccount = {},
            onToggleCategory = {},
            onExportClick = {},
            onNavigateToAdd = {},
            openMenuId = null,
            pendingDeleteId = null,
            onOpenMenu = { },
            onCloseMenu = {},
            onRequestDelete = {},
            onCancelDelete = {},
            onConfirmDelete = {},
            onToggleItem = {}
        )
    }
}

@Preview(name = "Transactions – Light", showBackground = true)
@Composable
fun TransactionsScreenPreviewLight() {
    ExpenseTheme(dark = false) {
        val sampleAccounts = listOf(
            Account(1, "Banco", AccountType.BANK, "ARS", 0),
            Account(2, "Efectivo", AccountType.CASH, "ARS", 0)
        )
        val sampleCategories = listOf(
            Category(1, "Comida", 1, 0xFFE57373, TxType.EXPENSE),
            Category(2, "Transporte", 2, 0xFF64B5F6, TxType.EXPENSE),
            Category(3, "Hogar", 3, 0xFF81C784, TxType.INCOME)
        )
        val sampleTx = listOf(
            Transaction(
                id = 1L, accountId = 1L, type = TxType.EXPENSE,
                amountMinor = 123000, date = LocalDateTime.now(),
                categoryId = 1L, merchant = "Café", note = null
            ),
            Transaction(
                id = 2L, accountId = 2L, type = TxType.EXPENSE,
                amountMinor = 12500, date = LocalDateTime.now(),
                categoryId = 2L, merchant = null, note = "si"
            )
        )

        val pagingItems = remember(sampleTx) {
            flowOf(PagingData.from(sampleTx))
        }.collectAsLazyPagingItems()

        TransactionsScreen(
            filter = TransactionFilter(),
            accounts = sampleAccounts,
            categories = sampleCategories,
            pagingItems = pagingItems,
            expandedId = null,
            filtersVisible = true,
            onToggleFilters = {},
            onQueryChange = {},
            onDateRangeChange = { _, _ -> },
            onToggleAccount = {},
            onToggleCategory = {},
            onExportClick = {},
            onNavigateToAdd = {},
            openMenuId = null,
            pendingDeleteId = null,
            onOpenMenu = { },
            onCloseMenu = {},
            onRequestDelete = {},
            onCancelDelete = {},
            onConfirmDelete = {},
            onToggleItem = {}
        )
    }
}