package com.warh.accounts.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.elevatedCardColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.warh.accounts.R
import com.warh.accounts.localized
import com.warh.accounts.utils.BalanceUtils.parseMinor
import com.warh.commons.NumberUtils.formatHeroAmount
import com.warh.commons.TopBarDefault
import com.warh.commons.bottom_bar.FabSpec
import com.warh.commons.color_picker.ColorChooser
import com.warh.commons.icons.IconGrid
import com.warh.designsystem.ExpenseTheme
import com.warh.designsystem.dropdown.DropdownColors
import com.warh.designsystem.dropdown.DropdownColors.DropdownMenuColors.selectedItemBackground
import com.warh.domain.models.AccountType
import kotlinx.coroutines.launch
import java.util.Currency
import java.util.Locale
import com.warh.commons.R.drawable as CommonDrawables

@Composable
fun AccountAddRoute(
    vm: AccountAddViewModel,
    setFab: (FabSpec?) -> Unit,
    onBack: () -> Unit
) {
    val ui by vm.ui.collectAsState()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val show: (String) -> Unit = { msg -> scope.launch { snack.showSnackbar(msg) } }

    SideEffect {
        setFab(
            FabSpec(
                visible = true,
                onClick = { vm.save(onError = show, onDone = onBack) }
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    text = stringResource(
                        if (ui.isSaving) R.string.account_add_save_button_loading
                        else R.string.account_add_save_button
                    )
                )
            }
        )
    }

    AddAccountScreen(
        ui = ui,
        onName       = vm::onName,
        onType       = vm::onType,
        onCurrency   = vm::onCurrency,
        onBalanceText= vm::onBalanceText,
        onIconIndex  = vm::onIconIndex,
        onIconColor  = vm::onIconColor,
        onBack       = onBack,
        snackBar     = snack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountScreen(
    ui: AccountAddUiState,
    onName: (String) -> Unit,
    onType: (AccountType) -> Unit,
    onCurrency: (String) -> Unit,
    onBalanceText: (String) -> Unit,
    onIconIndex: (Int) -> Unit,
    onIconColor: (Long?) -> Unit,
    onBack: () -> Unit,
    snackBar: SnackbarHostState
) {
    val scroll = rememberTopAppBarState()
    val sb = pinnedScrollBehavior(scroll)
    val scrollState = rememberScrollState()

    val initialMinor = remember(ui.balanceText, ui.currency) {
        runCatching { parseMinor(ui.balanceText, ui.currency) }.getOrDefault(0L)
    }

    val previewTotalMinor = remember(
        ui.isEditing, initialMinor, ui.originalInitialBalance, ui.originalBalance
    ) {
        if (ui.isEditing) {
            val delta = initialMinor - (ui.originalInitialBalance ?: 0L)
            (ui.originalBalance ?: 0L) + delta
        } else {
            initialMinor
        }
    }

    Scaffold(
        topBar = {
            TopBarDefault(
                title = stringResource(R.string.account_add_title),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                scrollBehavior = sb
            )
        },
        snackbarHost = { SnackbarHost(snackBar) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .nestedScroll(sb.nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroPreviewCard(
                name = ui.name.ifBlank { stringResource(R.string.account_add_new_placeholder) },
                type = ui.type,
                currency = ui.currency,
                totalMinor = previewTotalMinor,
                iconIndex = ui.iconIndex,
                iconColorArgb = ui.iconColorArgb
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ui.name,
                    onValueChange = onName,
                    label = { Text(stringResource(R.string.accounts_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                var typeExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                ) {
                    OutlinedTextField(
                        value = ui.type.localized(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.accounts_type_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth(),
                        colors = DropdownColors.dropdownTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        shape = DropdownColors.DropdownMenuColors.menuShape(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        shadowElevation = 8.dp
                    ) {
                        AccountType.entries.forEach { t ->
                            val selected = t == ui.type
                            DropdownMenuItem(
                                text = { Text(t.localized()) },
                                onClick = {
                                    onType(t)
                                    typeExpanded = false
                                },
                                colors = if (selected)
                                    DropdownColors.DropdownMenuColors.selectedItemColors()
                                else
                                    DropdownColors.DropdownMenuColors.itemColors(),
                                modifier = if (selected)
                                    Modifier.selectedItemBackground()
                                else
                                    Modifier
                            )
                        }
                    }
                }

                var curExpanded by remember { mutableStateOf(false) }
                CurrencyDropdown(
                    selected = ui.currency,
                    onSelect = onCurrency,
                    expanded = curExpanded,
                    onExpandedChange = { curExpanded = it }
                )

                val digits = remember(ui.currency) {
                    runCatching { Currency.getInstance(ui.currency).defaultFractionDigits }
                        .getOrDefault(2).coerceAtLeast(0)
                }

                OutlinedTextField(
                    value = ui.balanceText,
                    onValueChange = onBalanceText,
                    label = {
                        Text(stringResource(R.string.accounts_initial_balance_label))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (digits == 0) KeyboardType.Number else KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(stringResource(R.string.accounts_icon_label), style = MaterialTheme.typography.labelLarge)
                AccountIconGrid(
                    selectedNumber = ui.iconIndex,
                    onSelectNumber = onIconIndex
                )

                Text(
                    stringResource(R.string.accounts_color_label),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
                ColorChooser(
                    selected = ui.iconColorArgb,
                    onChange = onIconColor
                )

                Spacer(Modifier.height(8.dp))

                ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    selected: String,
    onSelect: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    val locale = remember { Locale.getDefault() }
    val all = remember { Currency.getAvailableCurrencies().map { it.currencyCode }.distinct().sorted() }
    val names = remember(all, locale) {
        all.associateWith { code -> runCatching { Currency.getInstance(code).getDisplayName(locale) }.getOrDefault(code) }
    }

    var query by remember { mutableStateOf("") }
    val filtered = remember(query, all, names) {
        val q = query.trim()
        if (q.isBlank()) all
        else {
            val byCode = all.filter { it.startsWith(q, ignoreCase = true) }
            val byName = all.filter { val n = names[it] ?: it; it !in byCode && n.startsWith(q, ignoreCase = true) }
            byCode + byName
        }
    }

    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val searchFR = remember { FocusRequester() }

    LaunchedEffect(expanded) {
        if (expanded) {
            searchFR.requestFocus()
            keyboard?.show()
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.accounts_currency_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
            colors = DropdownColors.dropdownTextFieldColors()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onExpandedChange(false)
                focusManager.clearFocus()
                keyboard?.hide()
            },
            modifier = Modifier.exposedDropdownSize(matchTextFieldWidth = true),
            properties = PopupProperties(focusable = true),
            shape = DropdownColors.DropdownMenuColors.menuShape(),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Box(Modifier.padding(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.accounts_currency_search)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.fillMaxWidth().focusRequester(searchFR)
                )
            }
            HorizontalDivider()
            filtered.take(50).forEach { code ->
                val selectedItem = code == selected
                val name = names[code] ?: code
                val label = if (!name.equals(code, ignoreCase = true)) "$code — $name" else code
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelect(code)
                        onExpandedChange(false)
                        focusManager.clearFocus()
                        keyboard?.hide()
                    },
                    leadingIcon = { if (selectedItem) Icon(Icons.Default.Check, null) },
                    colors = if (selectedItem)
                        DropdownColors.DropdownMenuColors.selectedItemColors()
                    else
                        DropdownColors.DropdownMenuColors.itemColors(),
                    modifier = if (selectedItem) Modifier.selectedItemBackground() else Modifier
                )
            }
        }
    }
}

@Composable
private fun HeroPreviewCard(
    name: String,
    type: AccountType,
    currency: String,
    totalMinor: Long,
    iconIndex: Int,
    iconColorArgb: Long?
) {
    val cs = MaterialTheme.colorScheme
    val iconIds = remember {
        listOf(
            CommonDrawables.account_icon_1, CommonDrawables.account_icon_2,
            CommonDrawables.account_icon_3, CommonDrawables.account_icon_4,
            CommonDrawables.account_icon_5, CommonDrawables.account_icon_6,
            CommonDrawables.account_icon_7, CommonDrawables.account_icon_8
        )
    }
    val idx = (iconIndex - 1).coerceIn(0, iconIds.lastIndex)
    val tint = iconColorArgb?.let { Color(it.toInt()) } ?: cs.onSurfaceVariant

    val totalText   = formatHeroAmount(totalMinor, currency, Locale.getDefault())
    val totalPositive = totalMinor >= 0

    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = elevatedCardColors(containerColor = cs.secondaryContainer),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                tonalElevation = 4.dp,
                modifier = Modifier.size(48.dp),
                color = cs.surface
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(iconIds[idx]),
                        contentDescription = null,
                        tint = tint
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(
                    type.localized(),
                    style = MaterialTheme.typography.labelMedium,
                    color = cs.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    totalText,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (totalPositive) Color(0xFF2E7D32) else cs.error
                )
                Text(
                    currency,
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun AccountIconGrid(
    selectedNumber: Int,
    onSelectNumber: (Int) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4
) {
    val icons = remember {
        listOf(
            CommonDrawables.account_icon_1, CommonDrawables.account_icon_2,
            CommonDrawables.account_icon_3, CommonDrawables.account_icon_4,
            CommonDrawables.account_icon_5, CommonDrawables.account_icon_6,
            CommonDrawables.account_icon_7, CommonDrawables.account_icon_8
        )
    }
    IconGrid(
        icons = icons,
        selectedIndex = (selectedNumber - 1).coerceIn(0, icons.lastIndex),
        onSelect = { onSelectNumber(it + 1) },
        modifier = modifier,
        columns = columns
    )
}

@Preview(showBackground = true)
@Composable
private fun AddAccountPreviewLight() {
    ExpenseTheme(dark = false) {
        AddAccountScreen(
            ui = AccountAddUiState(),
            onName = {}, onType = {}, onCurrency = {}, onBalanceText = {},
            onIconIndex = {}, onIconColor = {}, onBack = {}, snackBar = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddAccountPreviewDark() {
    ExpenseTheme(dark = true) {
        AddAccountScreen(
            ui = AccountAddUiState(),
            onName = {}, onType = {}, onCurrency = {}, onBalanceText = {},
            onIconIndex = {}, onIconColor = {}, onBack = {}, snackBar = SnackbarHostState()
        )
    }
}
