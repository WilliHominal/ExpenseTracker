package com.warh.transactions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.warh.commons.TopBarDefault
import com.warh.domain.models.Transaction
import com.warh.domain.models.TransactionFilter
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransactionsRoute(
    onAddClick: () -> Unit,
    vm: TransactionsViewModel = koinViewModel(),
    vmExport: TransactionsExportViewModel = koinViewModel()
) {
    val pagingItems = vm.paging(TransactionFilter()).collectAsLazyPagingItems()
    val filter = remember { TransactionFilter() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopBarDefault(
                title = stringResource(R.string.transactions_title),
                actions = {
                    IconButton(onClick = { vmExport.exportCsv(context, filter) }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.transactions_title))
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
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            items(
                count = pagingItems.itemCount,
                key = { index -> pagingItems.peek(index)?.id ?: index }
            ) { index ->
                val tx = pagingItems[index]
                if (tx != null) TransactionRow(tx)
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction) {
    ListItem(
        headlineContent = { Text(tx.merchant ?: tx.note ?: stringResource(R.string.transactions_transaction_default_name)) },
        supportingContent = { Text("${'$'}${tx.amountMinor / 100.0} • ${tx.date}") }
    )
    HorizontalDivider(thickness = 0.5.dp)
}