package com.warh.commons.dropdown

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.warh.commons.R
import com.warh.designsystem.dropdown.DropdownColors
import com.warh.designsystem.dropdown.DropdownColors.DropdownMenuColors.selectedItemBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdown(
    label: String,
    selectedId: Long?,
    items: List<Pair<Long?, String>>,
    onSelect: (Long?) -> Unit,
    nullLabel: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    val placeholder = nullLabel ?: stringResource(R.string.dropdown_select)
    val selectedText = items.firstOrNull { it.first == selectedId }?.second ?: placeholder

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth(),
            colors = DropdownColors.dropdownTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = DropdownColors.DropdownMenuColors.menuShape(),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            items.forEach { (id, name) ->
                val selected = id == selectedId
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    },
                    colors = if (selected)
                        DropdownColors.DropdownMenuColors.selectedItemColors()
                    else
                        DropdownColors.DropdownMenuColors.itemColors(),
                    modifier = if (selected) Modifier.selectedItemBackground() else Modifier
                )
            }
        }
    }
}