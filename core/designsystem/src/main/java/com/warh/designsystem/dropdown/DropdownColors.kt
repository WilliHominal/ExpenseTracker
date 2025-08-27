package com.warh.designsystem.dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object DropdownColors {
    @Composable
    fun dropdownTextFieldColors() = OutlinedTextFieldDefaults.colors(
        // Texto
        focusedTextColor   = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor  = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorTextColor     = MaterialTheme.colorScheme.onSurface,

        // Fondo
        focusedContainerColor   = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        disabledContainerColor  = MaterialTheme.colorScheme.surface,
        errorContainerColor     = MaterialTheme.colorScheme.surface,

        // Cursor
        cursorColor      = MaterialTheme.colorScheme.primary,
        errorCursorColor = MaterialTheme.colorScheme.error,

        // Selección de texto
        selectionColors = TextSelectionColors(
            handleColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        ),

        // Bordes
        focusedBorderColor   = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        disabledBorderColor  = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
        errorBorderColor     = MaterialTheme.colorScheme.error,

        // Iconos leading
        focusedLeadingIconColor   = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLeadingIconColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        errorLeadingIconColor     = MaterialTheme.colorScheme.error,

        // Iconos trailing
        focusedTrailingIconColor   = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTrailingIconColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        errorTrailingIconColor     = MaterialTheme.colorScheme.error,

        // Labels
        focusedLabelColor   = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLabelColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        errorLabelColor     = MaterialTheme.colorScheme.error,

        // Placeholders
        focusedPlaceholderColor   = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        disabledPlaceholderColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        errorPlaceholderColor     = MaterialTheme.colorScheme.error,

        // Supporting text
        focusedSupportingTextColor   = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledSupportingTextColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        errorSupportingTextColor     = MaterialTheme.colorScheme.error,

        // Prefijo
        focusedPrefixColor   = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPrefixColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledPrefixColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        errorPrefixColor     = MaterialTheme.colorScheme.error,

        // Sufijo
        focusedSuffixColor   = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledSuffixColor  = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
        errorSuffixColor     = MaterialTheme.colorScheme.error,
    )

    object DropdownMenuColors {
        // Forma del menú: sin esquinas arriba para que no “asome” el borde del TextField
        @Composable
        fun menuShape(): Shape = RoundedCornerShape(
            topStart = 0.dp, topEnd = 0.dp,
            bottomStart = 12.dp, bottomEnd = 12.dp
        )

        // Colores para ítems “normales”
        @Composable
        fun itemColors(): MenuItemColors = MenuDefaults.itemColors(
            textColor = MaterialTheme.colorScheme.onSurface,
            leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            trailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        )

        // Colores para ítems “seleccionados” (texto/íconos sobre secondaryContainer)
        @Composable
        fun selectedItemColors(): MenuItemColors = MenuDefaults.itemColors(
            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
            leadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            trailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            disabledTextColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.38f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.38f),
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.38f)
        )

        // Modifier para resaltar el ítem seleccionado con fondo
        @Composable
        fun Modifier.selectedItemBackground(): Modifier =
            this
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
    }
}
