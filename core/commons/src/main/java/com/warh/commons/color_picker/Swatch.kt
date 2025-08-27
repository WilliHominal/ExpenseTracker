package com.warh.commons.color_picker

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun Swatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    val outerSize = 36.dp
    val innerSize by animateDpAsState(
        targetValue = if (selected) 32.dp else 28.dp, label = ""
    )
    val elevation = if (selected) 8.dp else 1.dp

    Box(Modifier.size(outerSize), contentAlignment = Alignment.Center) {
        ElevatedCard(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(innerSize),
            colors = CardDefaults.elevatedCardColors(containerColor = color),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation)
        ) {
            if (selected) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Check, null,
                        tint = contentColorFor(color),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GradientCustomSwatch(
    selected: Boolean,
    onClick: () -> Unit,
    size: Int = 28
) {
    val outerSize = 36.dp
    val innerTarget = if (selected) (size + 4) else size
    val innerSize by animateDpAsState(innerTarget.dp, label = "")
    val elevation = if (selected) 8.dp else 1.dp
    val brush = remember {
        Brush.sweepGradient(
            listOf(
                Color(0xFFFF5252), Color(0xFFFF9800), Color(0xFFFFEB3B),
                Color(0xFF4CAF50), Color(0xFF00BCD4), Color(0xFF3F51B5),
                Color(0xFFE91E63), Color(0xFFFF5252)
            )
        )
    }

    Box(Modifier.size(outerSize), contentAlignment = Alignment.Center) {
        ElevatedCard(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(innerSize),
            colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(brush, CircleShape)
            ) {
                if (selected) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}