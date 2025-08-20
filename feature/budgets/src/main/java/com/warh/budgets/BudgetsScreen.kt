package com.warh.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warh.commons.TopBarDefault
import com.warh.domain.models.Budget
import com.warh.domain.models.Category
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetsRoute(vm: BudgetsViewModel = koinViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopBarDefault(
                title = stringResource(R.string.budgets_title_month_year, ui.month, ui.year),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { vm.openNew() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) { Text("+") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ui.items.isEmpty()) Text(stringResource(R.string.budgets_empty_state))
            ui.items.forEach { row ->
                BudgetRow(
                    row = row,
                    onEdit = { vm.openEdit(row.categoryId, row.limitMinor) },
                    onRemove = vm::remove
                )
            }
        }
    }

    if (ui.showDialog) BudgetDialog(
        categories = ui.categories,
        editing = ui.editing,
        onDismiss = vm::closeDialog,
        onConfirm = { cat, limit -> vm.save(cat, limit) }
    )
}

@Composable
private fun BudgetRow(row: BudgetRow, onEdit: () -> Unit, onRemove: (Long) -> Unit) {
    val nf = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    val spent = nf.format(row.spentMinor / 100.0)
    val limit = nf.format(row.limitMinor / 100.0)
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
                progress = { row.ratio.coerceAtMost(1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp))

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
    var amountText by remember(editing) { mutableStateOf(if (editing != null) (editing.limitMinor / 100.0).toString() else "") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val id = selectedId
                val limit = ((amountText.replace(',', '.').toDoubleOrNull() ?: 0.0) * 100).toLong()
                if (id != null && limit > 0) onConfirm(id, limit)
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
                        value = categories.firstOrNull { it.id == selectedId }?.name ?: stringResource(R.string.budgets_select),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.budgets_category_label)) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { c ->
                            DropdownMenuItem(text = { Text(c.name) }, onClick = { selectedId = c.id; expanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(stringResource(R.string.budgets_limit_label_example)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        }
    )
}