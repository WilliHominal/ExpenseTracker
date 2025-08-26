package com.warh.commons.icons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IconGrid(
    icons: List<Int>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 6,
    itemSize: Dp = 44.dp,
    rowSpacing: Dp = 8.dp,
    colSpacing: Dp = 12.dp,
    shape: Shape = CircleShape,
    selectedContainer: Color = MaterialTheme.colorScheme.primaryContainer,
    unselectedContainer: Color = MaterialTheme.colorScheme.surface,
    selectedTint: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    unselectedTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val rows = (icons.size + columns - 1) / columns
    Column(modifier, verticalArrangement = Arrangement.spacedBy(rowSpacing)) {
        repeat(rows) { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(colSpacing)) {
                repeat(columns) { c ->
                    val idx = r * columns + c
                    if (idx < icons.size) {
                        val isSel = selectedIndex == idx
                        ElevatedCard(
                            onClick = { onSelect(idx) },
                            shape = shape,
                            modifier = Modifier.size(itemSize),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (isSel) selectedContainer else unselectedContainer
                            )
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(icons[idx]),
                                    contentDescription = null,
                                    tint = if (isSel) selectedTint else unselectedTint
                                )
                            }
                        }
                    } else {
                        Box(Modifier.size(itemSize))
                    }
                }
            }
        }
    }
}