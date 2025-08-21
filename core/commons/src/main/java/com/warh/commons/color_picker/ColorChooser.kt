package com.warh.commons.color_picker

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
fun ColorChooser(
    selected: Long?,
    onChange: (Long?) -> Unit
) {
    val presets: List<Long> = listOf(0xFF1E88E5, 0xFF43A047, 0xFFFB8C00, 0xFFFF52520)

    var showPicker by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        Swatch(
            color = MaterialTheme.colorScheme.surface,
            selected = selected == null,
            onClick = { onChange(null) }
        )

        presets.forEach { argb ->
            Swatch(
                color = Color(argb.toInt()),
                selected = selected == argb,
                onClick = { onChange(argb) }
            )
        }

        GradientCustomSwatch(
            selected = selected != null && presets.none { it == selected },
            onClick = { showPicker = true }
        )
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