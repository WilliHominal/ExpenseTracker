package com.warh.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import com.warh.commons.NumberUtils
import com.warh.commons.TopBarDefault
import com.warh.commons.bottom_bar.FabSpec
import com.warh.commons.dropdown.AppDropdown
import com.warh.designsystem.ExpenseTheme
import com.warh.domain.models.Account
import com.warh.domain.models.AccountType
import com.warh.domain.models.Category
import com.warh.domain.models.TxType
import org.koin.androidx.compose.koinViewModel
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

@Composable
fun AddEditTransactionRoute(
    vm: AddEditTransactionViewModel,
    setFab: (FabSpec?) -> Unit,
    onSaved: () -> Unit,
    onBack: () -> Unit,
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    SideEffect {
        setFab(FabSpec(visible = true, onClick = { vm.save(onSaved) }) {
            Text(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = stringResource(
                    if (ui.isSaving) R.string.add_transaction_save_button_loading
                    else R.string.add_transaction_save_button
                )
            )
        })
    }

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
        onBack           = onBack
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
    onBack: () -> Unit,
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
        val digits = selectedCurrency.defaultFractionDigits.coerceAtLeast(0)

        val minor: Long? = ui.amountText
            .replace(',', '.')
            .toBigDecimalOrNull()
            ?.movePointRight(digits)
            ?.setScale(0, RoundingMode.HALF_UP)
            ?.let { runCatching { it.longValueExact() }.getOrNull() }

        if (minor != null) {
            NumberUtils.formatAmountWithSymbol(minor, selectedCurrency.currencyCode, trimZeroDecimals = true)
        } else {
            ""
        }
    }

    Scaffold(
        topBar = {
            TopBarDefault(
                title = stringResource(R.string.add_transaction_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.add_transaction_cd_back)
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
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

            AppDropdown(
                label = stringResource(R.string.add_transaction_account_label),
                selectedId = ui.accountId,
                items = ui.accounts.map { it.id to it.name },
                onSelect = { it?.let(onAccountChange) }
            )

            AppDropdown(
                label = stringResource(R.string.add_transaction_category_label),
                selectedId = ui.categoryId,
                items = ui.categories.map { it.id to it.name },
                onSelect = onCategoryChange,
                nullLabel = stringResource(R.string.add_transaction_category_none)
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
            onBack = {}
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
            onBack = {}
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
        Category(1, "Comida",   1, 0xFFE57373L, TxType.EXPENSE),
        Category(2, "Transporte", 2, 0xFF64B5F6L, TxType.EXPENSE),
        Category(3, "Hogar",    3, 0xFF81C784L, TxType.INCOME),
    ),
    merchantSuggestions = listOf("Café Martínez", "Starbucks", "Havanna")
)