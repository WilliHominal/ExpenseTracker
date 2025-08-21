package com.warh.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warh.commons.TopBarDefault
import com.warh.commons.color_picker.ColorChooser
import com.warh.designsystem.ExpenseTheme
import com.warh.domain.models.Category
import com.warh.domain.models.TxType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.warh.commons.R.drawable as CommonDrawables

//TODO: Color + icono como en cuentas
//TODO: Categorias segun tipo: income/expense

@Composable
fun CategoriesRoute(vm: CategoriesViewModel = koinViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    CategoriesScreen(
        ui = ui,
        onStartAdd = vm::startAdd,
        onName = vm::onName,
        onType = vm::onType,
        onIconIndex = vm::onIconIndex,
        onIconColor = vm::onIconColor,
        onCancel = vm::cancel,
        onSave = vm::save,
        onStartEdit = vm::startEdit,
        onRemove = vm::remove
    )
}

@Composable
fun CategoriesScreen(
    ui: CategoriesUiState,
    onStartAdd: () -> Unit,
    onName: (String) -> Unit,
    onType: (TxType) -> Unit,
    onIconIndex: (Int) -> Unit,
    onIconColor: (Long?) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onStartEdit: (Category) -> Unit,
    onRemove: (Long, (String) -> Unit) -> Unit,
) {
    val snackBar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopBarDefault(
                title = stringResource(R.string.categories_title)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartAdd,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) { Icon(Icons.Default.Add, null) }
        },
        snackbarHost = { SnackbarHost(snackBar) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {

            ui.draft?.let { d ->
                CategoryEditorCard(
                    draft = d,
                    onName = onName,
                    onType = onType,
                    onIconIndex = onIconIndex,
                    onIconColor = onIconColor,
                    onCancel = onCancel,
                    onSave = onSave
                )
                HorizontalDivider()
            }

            LazyColumn(Modifier.fillMaxSize()) {
                items(ui.items, key = { it.id }) { c ->
                    val iconIds = remember {
                        listOf(
                            CommonDrawables.account_icon_1, CommonDrawables.account_icon_2, CommonDrawables.account_icon_3, CommonDrawables.account_icon_4,
                            CommonDrawables.account_icon_5, CommonDrawables.account_icon_6, CommonDrawables.account_icon_7, CommonDrawables.account_icon_8,
                        )
                    }
                    val idx = (c.iconIndex - 1).coerceIn(0, iconIds.lastIndex)
                    val tint = c.iconColorArgb?.let { Color(it.toInt()) }
                        ?: MaterialTheme.colorScheme.onSurfaceVariant

                    ListItem(
                        leadingContent = {
                            Icon(painterResource(iconIds[idx]), contentDescription = null, tint = tint)
                        },
                        headlineContent = { Text(c.name) },
                        supportingContent = {
                            val typeText = when (c.type) {
                                TxType.EXPENSE -> stringResource(R.string.categories_type_expense)
                                TxType.INCOME  -> stringResource(R.string.categories_type_income)
                                else           -> "—"
                            }
                            Text(typeText)
                        },
                        trailingContent = {
                            Row {
                                TextButton(onClick = { onStartEdit(c) }) {
                                    Text(stringResource(R.string.categories_edit_button))
                                }
                                IconButton(onClick = {
                                    onRemove(c.id) { msg ->
                                        scope.launch { snackBar.showSnackbar(msg) }
                                    }
                                }) { Icon(Icons.Default.Delete, null) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { onStartEdit(c) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun CategoryEditorCard(
    draft: CategoryDraft,
    onName: (String) -> Unit,
    onType: (TxType) -> Unit,
    onIconIndex: (Int) -> Unit,
    onIconColor: (Long?) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Card(Modifier.fillMaxWidth().padding(12.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            OutlinedTextField(
                value = draft.name,
                onValueChange = onName,
                label = { Text(stringResource(R.string.categories_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Tipo (solo Expense/Income)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = draft.type == TxType.EXPENSE,
                    onClick = { onType(TxType.EXPENSE) },
                    label = { Text(stringResource(R.string.categories_type_expense)) }
                )
                FilterChip(
                    selected = draft.type == TxType.INCOME,
                    onClick = { onType(TxType.INCOME) },
                    label = { Text(stringResource(R.string.categories_type_income)) }
                )
            }

            Text(stringResource(R.string.categories_icon_label), style = MaterialTheme.typography.labelLarge)
            IconGrid(selected = draft.iconIndex, onSelect = onIconIndex)

            Text(stringResource(R.string.categories_color_label), style = MaterialTheme.typography.labelLarge)
            ColorChooser(selected = draft.iconColorArgb, onChange = onIconColor)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onCancel) { Text(stringResource(R.string.categories_cancel_button)) }
                Button(onClick = onSave, enabled = draft.name.isNotBlank()) { Text(stringResource(R.string.categories_save_button)) }
            }
        }
    }
}

@Composable
private fun IconGrid(selected: Int, onSelect: (Int) -> Unit) {
    val icons = listOf(
        CommonDrawables.account_icon_1, CommonDrawables.account_icon_2, CommonDrawables.account_icon_3, CommonDrawables.account_icon_4,
        CommonDrawables.account_icon_5, CommonDrawables.account_icon_6, CommonDrawables.account_icon_7, CommonDrawables.account_icon_8
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (col in 0 until 4) {
                    val idx = row * 4 + col
                    val number = idx + 1
                    val isSel = number == selected
                    ElevatedCard(
                        onClick = { onSelect(number) },
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(icons[idx]),
                                contentDescription = null,
                                tint = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Categories — List (Light)", showBackground = true)
@Composable
fun CategoriesScreenPreview_List_Light() {
    ExpenseTheme(dark = false) {
        CategoriesScreen(
            ui = uiListState(),
            onStartAdd = {},
            onName = {},
            onType = {},
            onIconIndex = {},
            onIconColor = {},
            onCancel = {},
            onSave = {},
            onStartEdit = {},
            onRemove = { _, _ -> }
        )
    }
}

@Preview(name = "Categories — List (Dark)", showBackground = true)
@Composable
fun CategoriesScreenPreview_List_Dark() {
    ExpenseTheme(dark = true) {
        CategoriesScreen(
            ui = uiListState(),
            onStartAdd = {},
            onName = {},
            onType = {},
            onIconIndex = {},
            onIconColor = {},
            onCancel = {},
            onSave = {},
            onStartEdit = {},
            onRemove = { _, _ -> }
        )
    }
}

@Preview(name = "Categories — Draft (Light)", showBackground = true)
@Composable
fun CategoriesScreenPreview_Draft_Light() {
    ExpenseTheme(dark = false) {
        CategoriesScreen(
            ui = uiDraftState(),
            onStartAdd = {},
            onName = {},
            onType = {},
            onIconIndex = {},
            onIconColor = {},
            onCancel = {},
            onSave = {},
            onStartEdit = {},
            onRemove = { _, _ -> }
        )
    }
}

@Preview(name = "Categories — Draft (Dark)", showBackground = true)
@Composable
fun CategoriesScreenPreview_Draft_Dark() {
    ExpenseTheme(dark = true) {
        CategoriesScreen(
            ui = uiDraftState(),
            onStartAdd = {},
            onName = {},
            onType = {},
            onIconIndex = {},
            onIconColor = {},
            onCancel = {},
            onSave = {},
            onStartEdit = {},
            onRemove = { _, _ -> }
        )
    }
}

private fun sampleCategories(): List<Category> = listOf(
    Category(1, "Comida",    1, 0xFFE57373, TxType.EXPENSE),
    Category(2, "Transporte",2, 0xFF64B5F6, TxType.EXPENSE),
    Category(3, "Hogar",     3, 0xFF81C784, TxType.INCOME),
    Category(4, "Ocio",      4, 0xFFFFB74D, TxType.INCOME),
)

private fun uiListState() = CategoriesUiState(
    items = sampleCategories(),
    draft = null,
    error = null
)

private fun uiDraftState() = CategoriesUiState(
    items = sampleCategories(),
    draft = CategoryDraft(
        id = null,
        name = "Nueva categoría",
        type = TxType.EXPENSE,
        iconIndex = 1,
        iconColorArgb = null
    ),
    error = null
)