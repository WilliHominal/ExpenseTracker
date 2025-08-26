package com.warh.accounts.add

import androidx.compose.foundation.text.KeyboardOptions
import com.warh.accounts.localized
import com.warh.commons.TopBarDefault
import com.warh.commons.icons.IconGrid
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.elevatedCardColors
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.warh.designsystem.ExpenseTheme
import com.warh.domain.models.AccountType
import com.warh.accounts.R
import com.warh.commons.color_picker.ColorChooser
import com.warh.accounts.utils.BalanceUtils.parseMinor
import com.warh.accounts.utils.BalanceUtils.formatMajor
import com.warh.commons.bottom_bar.FabSpec
import java.util.Currency
import com.warh.commons.R.drawable as CommonDrawables

@Composable
fun AccountAddRoute(
    vm: AccountAddViewModel = koinViewModel(),
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
                balanceText = ui.balanceText,
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
                    onExpandedChange = { typeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = ui.type.localized(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.accounts_type_label)) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        AccountType.entries.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.localized()) },
                                onClick = { onType(t); typeExpanded = false }
                            )
                        }
                    }
                }

                val currencies = listOf("ARS","USD","EUR","BRL","CLP","UYU","MXN")
                var curExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = curExpanded,
                    onExpandedChange = { curExpanded = it }
                ) {
                    OutlinedTextField(
                        value = ui.currency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.accounts_currency_label)) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = curExpanded,
                        onDismissRequest = { curExpanded = false }
                    ) {
                        currencies.forEach { code ->
                            DropdownMenuItem(
                                text = { Text(code) },
                                onClick = { onCurrency(code); curExpanded = false }
                            )
                        }
                    }
                }

                val digits = remember(ui.currency) {
                    runCatching { Currency.getInstance(ui.currency).defaultFractionDigits }
                        .getOrDefault(2).coerceAtLeast(0)
                }

                OutlinedTextField(
                    value = ui.balanceText,
                    onValueChange = onBalanceText,
                    label = {
                        Text(stringResource(R.string.accounts_initial_balance_label, ui.currency))
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

@Composable
private fun HeroPreviewCard(
    name: String,
    type: AccountType,
    currency: String,
    balanceText: String,
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

    val parsed = runCatching { parseMinor(balanceText, currency) }.getOrDefault(0L)
    val formatted = formatMajor(parsed, currency)
    val positive = parsed >= 0

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
                    formatted,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (positive) Color(0xFF2E7D32) else cs.error
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
