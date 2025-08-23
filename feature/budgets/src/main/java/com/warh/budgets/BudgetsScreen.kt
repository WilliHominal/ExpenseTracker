package com.warh.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warh.commons.NumberUtils
import com.warh.commons.TopBarDefault
import com.warh.commons.bottom_bar.FabSpec
import com.warh.commons.bottom_bar.LocalBottomBarBehavior
import com.warh.domain.models.Budget
import com.warh.domain.models.Category
import org.koin.androidx.compose.koinViewModel
import java.math.RoundingMode
import java.util.Currency
import java.util.Locale

@Composable
fun BudgetsRoute(
    vm: BudgetsViewModel = koinViewModel(),
    setFab: (FabSpec?) -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    SideEffect {
        setFab(FabSpec(visible = true, onClick = { vm.openNew() }) {
            Icon(Icons.Default.Add, null)
        })
    }

    BudgetsScreen(
        ui = ui,
        onOpenEdit = { id, limit -> vm.openEdit(id, limit) },
        onRemove = vm::remove,
        onCloseDialog = vm::closeDialog,
        onConfirmDialog = { cat, limit -> vm.save(cat, limit) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetsScreen(
    ui: BudgetsUiState,
    onOpenEdit: (categoryId: Long, limitMinor: Long) -> Unit,
    onRemove: (categoryId: Long) -> Unit,
    onCloseDialog: () -> Unit,
    onConfirmDialog: (categoryId: Long, limitMinor: Long) -> Unit
) {
    val layoutDir = LocalLayoutDirection.current
    val appBarState = rememberTopAppBarState()
    val topSb  = TopAppBarDefaults.enterAlwaysScrollBehavior(appBarState)
    val bottomSb = LocalBottomBarBehavior.current

    Scaffold(
        topBar = {
            TopBarDefault(
                title = stringResource(R.string.budgets_title_month_year, ui.month, ui.year),
                scrollBehavior = topSb
            )
        },
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = inner.calculateStartPadding(layoutDir),
                    top = inner.calculateTopPadding(),
                    end = inner.calculateEndPadding(layoutDir),
                    bottom = 0.dp,
                )
                .nestedScroll(topSb.nestedScrollConnection)
                .then(bottomSb?.let { Modifier.nestedScroll(it.connection) } ?: Modifier),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (ui.items.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.budgets_empty_state),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(ui.items, key = { it.categoryId }) { row ->
                    BudgetRow(
                        row = row,
                        onEdit = { onOpenEdit(row.categoryId, row.limitMinor) },
                        onRemove = onRemove
                    )
                }
            }
        }
    }

    if (ui.showDialog) {
        BudgetDialog(
            categories = ui.categories,
            editing = ui.editing,
            onDismiss = onCloseDialog,
            onConfirm = onConfirmDialog
        )
    }
}

@Composable
private fun BudgetRow(row: BudgetRow, onEdit: () -> Unit, onRemove: (Long) -> Unit) {
    val currencyCode = remember { Currency.getInstance(Locale.getDefault()).currencyCode }

    val spent = remember(row.spentMinor) {
        NumberUtils.formatAmountWithSymbol(row.spentMinor, currencyCode, trimZeroDecimals = true)
    }
    val limit = remember(row.limitMinor) {
        NumberUtils.formatAmountWithSymbol(row.limitMinor, currencyCode, trimZeroDecimals = true)
    }

    val over = row.spentMinor > row.limitMinor
    val warn = !over && row.ratio >= 0.8f

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = row.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f))

                var menu by remember { mutableStateOf(false) }

                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.budgets_edit)) },
                        onClick = { menu = false; onEdit() })
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.budgets_delete)) },
                        onClick = { menu = false; onRemove(row.categoryId) })
                }
            }

            LinearProgressIndicator(
                progress = { row.ratio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = when {
                    row.spentMinor > row.limitMinor -> MaterialTheme.colorScheme.error
                    row.ratio >= 0.8f               -> MaterialTheme.colorScheme.tertiary
                    else                            -> MaterialTheme.colorScheme.primary
                }
            )

            val progressText = stringResource(R.string.budgets_progress_value, spent, limit) +
                    when {
                        over -> stringResource(R.string.budgets_over_suffix)
                        warn -> stringResource(R.string.budgets_warn_suffix_percent, 80)
                        else -> ""
                    }
            Text(progressText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BudgetDialog(categories: List<Category>, editing: Budget?, onDismiss: () -> Unit, onConfirm: (Long, Long) -> Unit) {
    var selectedId by remember(editing) { mutableStateOf(editing?.categoryId) }

    val currency = remember { Currency.getInstance(Locale.getDefault()) }
    val digits = currency.defaultFractionDigits.coerceAtLeast(0)

    var amountText by remember(editing) {
        val initial = editing?.limitMinor
            ?.let { minor ->
                val bd = java.math.BigDecimal(minor).movePointLeft(digits)
                bd.stripTrailingZeros().toPlainString()
            } ?: ""
        mutableStateOf(initial)
    }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val id = selectedId
                val minor = amountText
                    .replace(',', '.')
                    .toBigDecimalOrNull()
                    ?.movePointRight(digits)
                    ?.setScale(0, RoundingMode.HALF_UP)
                    ?.let { runCatching { it.longValueExact() }.getOrNull() }
                    ?: 0L
                if (id != null && minor > 0) onConfirm(id, minor)
            }) {
                Text(stringResource(if (editing == null) R.string.budgets_create else R.string.budgets_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.budgets_cancel))
            }
        },
        title = {
            Text(text = stringResource(if (editing == null) R.string.budgets_dialog_title_new else R.string.budgets_dialog_title_edit))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = categories.firstOrNull { it.id == selectedId }?.name
                            ?: stringResource(R.string.budgets_select),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.budgets_category_label)) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = { selectedId = c.id; expanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        val filtered = raw.filter { it.isDigit() || it == '.' || it == ',' }
                        val normalized = filtered.replace(',', '.')
                        val parts = normalized.split('.', limit = 2)
                        amountText = if (digits == 0) {
                            parts[0].takeIf { it.isNotEmpty() } ?: ""
                        } else {
                            if (parts.size == 1) {
                                parts[0]
                            } else {
                                val intPart = parts[0].ifEmpty { "0" }
                                val fracPart = parts[1].take(digits)
                                if (fracPart.isEmpty()) "$intPart." else "$intPart.$fracPart"
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.budgets_limit_label_example)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (digits == 0) KeyboardType.Number else KeyboardType.Decimal
                    ),
                )
            }
        }
    )
}