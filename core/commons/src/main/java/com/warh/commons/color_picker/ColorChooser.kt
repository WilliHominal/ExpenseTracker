package com.warh.commons.color_picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
fun ColorChooser(
    selected: Long?,
    onChange: (Long?) -> Unit
) {
    val presets: List<Long> = listOf(
        0xFF1E88E5L, 0xFF43A047L, 0xFFFB8C00L, 0xFFFF5252L
    )

    var showPicker by remember { mutableStateOf(false) }

    LazyRow(
        contentPadding = PaddingValues(0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item(key = "none") {
            Swatch(
                color = MaterialTheme.colorScheme.surface,
                selected = selected == null,
                onClick = { onChange(null) }
            )
        }

        items(presets, key = { it }) { argb ->
            Swatch(
                color = Color(argb.toInt()),
                selected = selected == argb,
                onClick = { onChange(argb) }
            )
        }

        item(key = "custom") {
            GradientCustomSwatch(
                selected = selected != null && presets.none { it == selected },
                onClick = { showPicker = true }
            )
        }
    }

    if (showPicker) {
        RingColorPickerDialog(
            initial = selected?.let { Color(it.toInt()) } ?: MaterialTheme.colorScheme.primary,
            onCancel = { showPicker = false },
            onPick = { c ->
                onChange(c.toArgb().toLong())
                showPicker = false
            }
        )
    }
}