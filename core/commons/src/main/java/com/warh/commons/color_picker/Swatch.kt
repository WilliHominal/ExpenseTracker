package com.warh.commons.color_picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Swatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = color),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (selected) 6.dp else 1.dp)
    ) {}
}

@Composable
fun GradientCustomSwatch(
    selected: Boolean,
    onClick: () -> Unit,
    size: Int = 28
) {
    val brush = remember {
        Brush.sweepGradient(
            listOf(
                Color(0xFFFF5252),
                Color(0xFFFF9800),
                Color(0xFFFFEB3B),
                Color(0xFF4CAF50),
                Color(0xFF00BCD4),
                Color(0xFF3F51B5),
                Color(0xFFE91E63),
                Color(0xFFFF5252)
            )
        )
    }

    ElevatedCard(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(size.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (selected) 6.dp else 1.dp
        )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(brush, CircleShape)
        )
    }
}