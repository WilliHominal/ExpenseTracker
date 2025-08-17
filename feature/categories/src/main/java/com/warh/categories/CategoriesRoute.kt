package com.warh.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CategoriesRoute(vm: CategoriesViewModel = koinViewModel()) {
    val ui by vm.ui.collectAsState()
    val snackBar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = vm::startAdd,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) { Icon(Icons.Default.Add, null) } },
        snackbarHost = { SnackbarHost(snackBar) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {

            ui.draft?.let { d ->
                CategoryEditorCard(
                    draft = d,
                    onName = vm::onName,
                    onColor = vm::onColor,
                    onCancel = vm::cancel,
                    onSave = vm::save
                )
                HorizontalDivider()
            }

            LazyColumn(Modifier.fillMaxSize()) {
                items(ui.items, key = { it.id }) { c ->
                    ListItem(
                        leadingContent = {
                            Surface(color = Color(c.colorArgb.toInt()), shape = MaterialTheme.shapes.small, modifier = Modifier.size(16.dp)) {}
                        },
                        headlineContent = { Text(c.name) },
                        trailingContent = {
                            Row {
                                TextButton(onClick = { vm.startEdit(c) }) { Text(stringResource(R.string.categories_edit_button)) }
                                IconButton(onClick = {
                                    vm.remove(c.id) { msg -> scope.launch { snackBar.showSnackbar(msg) } }
                                }) { Icon(Icons.Default.Delete, null) }
                            }
                        }
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
    onColor: (Long) -> Unit,
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

            Text(stringResource(R.string.categories_color_label))
            ColorGrid(
                selected = draft.colorArgb,
                onSelect = onColor
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.align(Alignment.End)) {
                TextButton(onClick = onCancel) { Text(stringResource(R.string.categories_cancel_button)) }
                Button(onClick = onSave, enabled = draft.name.isNotBlank()) { Text(stringResource(R.string.categories_save_button)) }
            }
        }
    }
}

@Composable
private fun ColorGrid(selected: Long, onSelect: (Long) -> Unit) {
    val colors = listOf(
        0xFFEF5350, 0xFFAB47BC, 0xFF5C6BC0, 0xFF29B6F6, 0xFF26A69A, 0xFF66BB6A,
        0xFFFFCA28, 0xFFFFA726, 0xFFFF7043, 0xFF8D6E63, 0xFF9E9E9E, 0xFF607D8B
    ).map { it or 0xFF000000 }

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        colors.forEach { argb ->
            val sel = argb == selected
            Surface(
                color = Color(argb.toInt()),
                shape = MaterialTheme.shapes.small,
                tonalElevation = if (sel) 6.dp else 0.dp,
                shadowElevation = if (sel) 6.dp else 0.dp,
                modifier = Modifier
                    .size(if (sel) 28.dp else 24.dp)
                    .padding(2.dp)
                    .clickable { onSelect(argb) }
            ) {}
        }
    }
}