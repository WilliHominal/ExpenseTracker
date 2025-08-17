package com.warh.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.warh.commons.TopBarDefault
import com.warh.domain.models.TxType
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionRoute(
    onSaved: () -> Unit,
    vm: AddEditTransactionViewModel = koinViewModel()
) {
    val ui by vm.ui.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    val dateText = remember(ui.date) { ui.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }

    val currencyPreview = remember(ui.amountText) {
        val norm = ui.amountText.replace(',', '.')
        val d = norm.toDoubleOrNull()
        if (d != null) NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            currency = Currency.getInstance(Locale.getDefault())
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }.format(d) else ""
    }

    Scaffold(
        topBar = {
            TopBarDefault(
                title = stringResource(R.string.add_transaction_title),
            )
         },
        bottomBar = {
            BottomAppBar(actions = {
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { vm.save(onSaved) },
                    enabled = !ui.isSaving,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = stringResource(
                            if (ui.isSaving) R.string.add_transaction_save_button_loading
                            else R.string.add_transaction_save_button))
                }
            })
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = ui.amountText,
                onValueChange = vm::onAmountChange,
                label = { Text(stringResource(R.string.add_transaction_amount_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                supportingText = { if (currencyPreview.isNotEmpty()) Text(currencyPreview) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = ui.type == TxType.EXPENSE,
                    onClick = { vm.onTypeChange(TxType.EXPENSE) },
                    label = { Text(stringResource(R.string.add_transaction_tx_type_expense)) }
                )
                FilterChip(
                    selected = ui.type == TxType.INCOME,
                    onClick = { vm.onTypeChange(TxType.INCOME) },
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
                onSelected = vm::onAccountChange
            )

            ExposedDropdown(
                label = stringResource(R.string.add_transaction_category_label),
                items = ui.categories.map { it.id to it.name },
                selectedId = ui.categoryId,
                onSelected = { vm.onCategoryChange(it) }
            )

            MerchantAutocompleteField(
                value = ui.merchant,
                suggestions = ui.merchantSuggestions,
                onValue = vm::onMerchantChange,
                onPick = vm::onMerchantPick,
            )

            OutlinedTextField(
                value = ui.note,
                onValueChange = vm::onNoteChange,
                label = { Text(stringResource(R.string.add_transaction_note_label)) },
                modifier = Modifier.fillMaxWidth()
            )

            if (ui.error != null) Text(ui.error!!, color = MaterialTheme.colorScheme.error)
        }

        if (showDatePicker) {
            val initialMillis = remember(ui.date) { ui.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }
            val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val millis = state.selectedDateMillis
                        if (millis != null) {
                            val ldt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
                            vm.onDateChange(ldt)
                        }
                        showDatePicker = false
                    }) { Text(stringResource(R.string.add_transaction_ok)) }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.add_transaction_cancel)) }
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

        androidx.compose.material3.DropdownMenu(
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