package com.warh.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warh.commons.TopBarDefault
import com.warh.designsystem.ExpenseTheme
import com.warh.domain.models.Account
import com.warh.domain.models.AccountType
import com.warh.domain.models.Category
import com.warh.domain.models.TxType
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

@Composable
fun AddEditTransactionRoute(
    onSaved: () -> Unit,
    vm: AddEditTransactionViewModel = koinViewModel()
) {

    val ui by vm.ui.collectAsStateWithLifecycle()

    AddEditTransactionScreen(
        ui = ui,
        onAmountChange   = vm::onAmountChange,
        onTypeChange     = vm::onTypeChange,
        onAccountChange  = vm::onAccountChange,
        onCategoryChange = vm::onCategoryChange,
        onMerchantChange = vm::onMerchantChange,
        onMerchantPick   = vm::onMerchantPick,
        onNoteChange     = vm::onNoteChange,
        onDateChange     = vm::onDateChange,
        onSave           = { vm.save(onSaved) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    ui: TxEditorUiState,
    onAmountChange: (String) -> Unit,
    onTypeChange: (TxType) -> Unit,
    onAccountChange: (Long) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onMerchantChange: (String) -> Unit,
    onMerchantPick: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateChange: (LocalDateTime) -> Unit,
    onSave: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateText = remember(ui.date) {
        ui.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    val selectedCurrency: Currency = remember(ui.accountId, ui.accounts) {
        val code = ui.accounts.firstOrNull { it.id == ui.accountId }?.currency
            ?: Currency.getInstance(Locale.getDefault()).currencyCode
        runCatching { Currency.getInstance(code) }
            .getOrElse { Currency.getInstance(Locale.getDefault()) }
    }

    val currencyPreview = remember(ui.amountText, selectedCurrency) {
        val d = ui.amountText.replace(',', '.').toDoubleOrNull()
        if (d != null) {
            val digits = selectedCurrency.defaultFractionDigits.coerceAtLeast(0)
            NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
                currency = selectedCurrency
                maximumFractionDigits = digits
                minimumFractionDigits = digits
            }.format(d)
        } else ""
    }

    Scaffold(
        topBar = { TopBarDefault(title = stringResource(R.string.add_transaction_title)) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSave,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = stringResource(
                        if (ui.isSaving) R.string.add_transaction_save_button_loading
                        else R.string.add_transaction_save_button
                    )
                )
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val keyboardType =
                if (selectedCurrency.defaultFractionDigits == 0) KeyboardType.Number
                else KeyboardType.Decimal

            OutlinedTextField(
                value = ui.amountText,
                onValueChange = onAmountChange,
                label = { Text(stringResource(R.string.add_transaction_amount_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                supportingText = { if (currencyPreview.isNotEmpty()) Text(currencyPreview) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = ui.type == TxType.EXPENSE,
                    onClick = { onTypeChange(TxType.EXPENSE) },
                    label = { Text(stringResource(R.string.add_transaction_tx_type_expense)) }
                )
                FilterChip(
                    selected = ui.type == TxType.INCOME,
                    onClick = { onTypeChange(TxType.INCOME) },
                    label = { Text(stringResource(R.string.add_transaction_tx_type_income)) }
                )
            }

            TextButton(onClick = { showDatePicker = true }) {
                Text(stringResource(R.string.add_transaction_date_button, dateText))
            }

            ExposedDropdown(
                label = stringResource(R.string.add_transaction_account_label),
                items = ui.accounts.map { it.id to it.name },
                selectedId = ui.accountId,
                onSelected = onAccountChange
            )

            ExposedDropdown(
                label = stringResource(R.string.add_transaction_category_label),
                items = ui.categories.map { it.id to it.name },
                selectedId = ui.categoryId,
                onSelected = onCategoryChange
            )

            MerchantAutocompleteField(
                value = ui.merchant,
                suggestions = ui.merchantSuggestions,
                onValue = onMerchantChange,
                onPick = onMerchantPick,
            )

            OutlinedTextField(
                value = ui.note,
                onValueChange = onNoteChange,
                label = { Text(stringResource(R.string.add_transaction_note_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            if (ui.error != null) Text(ui.error, color = MaterialTheme.colorScheme.error)
        }

        if (showDatePicker) {
            val initialMillis = remember(ui.date) {
                ui.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val ldt = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(millis), ZoneId.systemDefault()
                            )
                            onDateChange(ldt)
                        }
                        showDatePicker = false
                    }) { Text(stringResource(R.string.add_transaction_ok)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.add_transaction_cancel))
                    }
                }
            ) { DatePicker(state = state) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdown(
    label: String,
    items: List<Pair<Long, String>>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = items.firstOrNull { it.first == selectedId }?.second ?: stringResource(R.string.add_transaction_select)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { (id, name) ->
                DropdownMenuItem(text = { Text(name) }, onClick = {
                    onSelected(id)
                    expanded = false
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MerchantAutocompleteField(
    value: String,
    suggestions: List<String>,
    onValue: (String) -> Unit,
    onPick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }

    LaunchedEffect(suggestions, hasFocus) {
        expanded = hasFocus && suggestions.isNotEmpty()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (hasFocus) expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValue,
            label = { Text(stringResource(R.string.add_transaction_merchant_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = hasFocus)
                .fillMaxWidth()
                .onFocusChanged { hasFocus = it.isFocused }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false),
            modifier = Modifier.exposedDropdownSize()
        ) {
            suggestions.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s) },
                    onClick = {
                        onPick(s)
                        expanded = false
                    }
                )
            }
        }
    }
}
@Preview(name = "Add/Edit Tx — Light", showBackground = true)
@Composable
fun AddEditTransactionScreenPreviewLight() {
    ExpenseTheme(dark = false) {
        AddEditTransactionScreen(
            ui = previewTxEditorUi(),
            onAmountChange = {},
            onTypeChange = {},
            onAccountChange = {},
            onCategoryChange = {},
            onMerchantChange = {},
            onMerchantPick = {},
            onNoteChange = {},
            onDateChange = {},
            onSave = {}
        )
    }
}

@Preview(name = "Add/Edit Tx — Dark", showBackground = true)
@Composable
fun AddEditTransactionScreenPreviewDark() {
    ExpenseTheme(dark = true) {
        AddEditTransactionScreen(
            ui = previewTxEditorUi(),
            onAmountChange = {},
            onTypeChange = {},
            onAccountChange = {},
            onCategoryChange = {},
            onMerchantChange = {},
            onMerchantPick = {},
            onNoteChange = {},
            onDateChange = {},
            onSave = {}
        )
    }
}

private fun previewTxEditorUi() = TxEditorUiState(
    amountText = "123",
    type = TxType.EXPENSE,
    accountId = 1L,
    categoryId = 2L,
    merchant = "Café Martínez",
    note = "Latte y medialuna",
    date = LocalDateTime.now(),
    isSaving = false,
    error = null,
    accounts = listOf(
        Account(1, "Banco", AccountType.BANK, "USD", 0),
        Account(2, "Efectivo", AccountType.CASH, "ARS", 0),
    ),
    categories = listOf(
        Category(1, "Comida",   0xFFE57373L),
        Category(2, "Transporte", 0xFF64B5F6L),
        Category(3, "Hogar",    0xFF81C784L),
    ),
    merchantSuggestions = listOf("Café Martínez", "Starbucks", "Havanna")
)