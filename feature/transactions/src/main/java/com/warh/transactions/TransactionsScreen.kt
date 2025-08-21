package com.warh.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.warh.commons.DateUtils.formatDateTime
import com.warh.commons.NumberUtils.formatAmountWithCode
import com.warh.commons.TopBarDefault
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

//TODO: Mostrar separador por día
//TODO: Transacciones recurrentes o programadas, en cantidad o porcentaje (tipo plazo fijo)

@Composable
fun TransactionsRoute(
    onAddClick: () -> Unit,
    vm: TransactionsViewModel = koinViewModel(),
    vmExport: TransactionsExportViewModel = koinViewModel()
) {
    val pagingItems = vm.paging.collectAsLazyPagingItems()
    val filter by vm.filter.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val filtersVisible by vm.filtersVisible.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
        onAddClick = onAddClick,
        onExportClick = { vmExport.exportCsv(context, filter) }
    )
}

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
    onAddClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Scaffold(
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
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) { Text("+") }
        }
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

            LazyColumn(Modifier.fillMaxSize()) {
                items(
                    count = pagingItems.itemCount,
                    key = { idx -> pagingItems.peek(idx)?.id ?: idx }
                ) { idx ->
                    pagingItems[idx]?.let { tx ->
                        TransactionRow(tx)
                    }
                }
            }
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
                    onClick = { onToggleAccount(acc.id) },
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
private fun TransactionRow(tx: Transaction) {
    val amountText = remember(tx.amountMinor, tx.currency) {
        formatAmountWithCode(tx.amountMinor, tx.currency)
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
            Category(1, "Comida", 0xFFE57373),
            Category(2, "Transporte", 0xFF64B5F6),
            Category(3, "Hogar", 0xFF81C784)
        )
        val sampleTx = listOf(
            Transaction(
                id = 1L, accountId = 1L, type = TxType.EXPENSE,
                amountMinor = 123000, currency = "ARS",
                date = LocalDateTime.now(), categoryId = 1L, merchant = "Café", note = null
            ),
            Transaction(
                id = 2L, accountId = 2L, type = TxType.EXPENSE,
                amountMinor = 12500, currency = "ARS",
                date = LocalDateTime.now(), categoryId = 2L, merchant = null, note = "si"
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
            filtersVisible = true,
            onToggleFilters = {},
            onQueryChange = {},
            onDateRangeChange = { _, _ -> },
            onToggleAccount = {},
            onToggleCategory = {},
            onAddClick = {},
            onExportClick = {}
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
            Category(1, "Comida", 0xFFE57373),
            Category(2, "Transporte", 0xFF64B5F6),
            Category(3, "Hogar", 0xFF81C784)
        )
        val sampleTx = listOf(
            Transaction(
                id = 1L, accountId = 1L, type = TxType.EXPENSE,
                amountMinor = 123000, currency = "ARS",
                date = LocalDateTime.now(), categoryId = 1L, merchant = "Café", note = null
            ),
            Transaction(
                id = 2L, accountId = 2L, type = TxType.EXPENSE,
                amountMinor = 12500, currency = "ARS",
                date = LocalDateTime.now(), categoryId = 2L, merchant = null, note = "si"
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
            filtersVisible = true,
            onToggleFilters = {},
            onQueryChange = {},
            onDateRangeChange = { _, _ -> },
            onToggleAccount = {},
            onToggleCategory = {},
            onAddClick = {},
            onExportClick = {}
        )
    }
}