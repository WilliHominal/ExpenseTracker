package com.warh.expensetracker

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.warh.commons.scroll_utils.collapseBy
import com.warh.designsystem.bottom_bar.BottomBarDesign
import kotlin.math.roundToInt

data class BarItem(val route: String, val icon: ImageVector)

@Composable
fun BottomBar(
    nav: NavHostController,
    currentRoute: String?,
    offsetY: Float,
    onMeasuredHeight: (Int) -> Unit,
) {
    val navInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)

    val items = remember {
        listOf(
            BarItem(Destinations.TRANSACTIONS, Icons.Default.History),
            BarItem(Destinations.BUDGETS, Icons.Default.PieChart),
            BarItem(Destinations.CATEGORIES, Icons.Default.Category),
            BarItem(Destinations.ACCOUNTS, Icons.Default.AccountBalance),
        )
    }
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val density = LocalDensity.current
    val rowHeightPx = with(density) {
        (BottomBarDesign.Sizes.RowHeight + BottomBarDesign.Sizes.PadTop + BottomBarDesign.Sizes.PadBottom).roundToPx()
    }
    val navBottomPx = navInsets.getBottom(density)

    val maxCollapsePx = rowHeightPx + navBottomPx
    val t = (offsetY / maxCollapsePx).coerceIn(0f, 1f)
    val topPadPx = (navBottomPx * t).roundToInt()
    val bottomPadPx = navBottomPx - topPadPx
    val topPad = with(density) { topPadPx.toDp() }
    val bottomPad = with(density) { bottomPadPx.toDp() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BottomBarDesign.Colors.containerColor())
            .clipToBounds()
            .collapseBy(offsetY, minHeightPx = navBottomPx)
            .onSizeChanged { onMeasuredHeight(rowHeightPx + navBottomPx) }
    ) {
        Column(Modifier.fillMaxWidth()) {
            Spacer(Modifier.height(topPad))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BottomBarDesign.Sizes.RowHeight)
                    .padding(
                        PaddingValues(
                            start = BottomBarDesign.Sizes.PadStartEnd,
                            end = BottomBarDesign.Sizes.PadStartEnd,
                            top = BottomBarDesign.Sizes.PadTop,
                            bottom = BottomBarDesign.Sizes.PadBottom
                        )
                    )
            ) {
                var contentWidthPx by remember { mutableIntStateOf(0) }
                val pillWidthPx = with(density) { BottomBarDesign.Sizes.PillWidth.roundToPx() }

                val slotWidthPx = if (contentWidthPx > 0) contentWidthPx / items.size else 0
                val targetX = (slotWidthPx * selectedIndex) + (slotWidthPx - pillWidthPx) / 2
                val animX by animateIntAsState(
                    targetValue = targetX,
                    animationSpec = tween(
                        durationMillis = BottomBarDesign.Anim.SLIDE_MS,
                        easing = BottomBarDesign.Anim.slideEasing
                    )
                )

                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .offset { IntOffset(animX, 0) }
                        .size(BottomBarDesign.Sizes.PillWidth, BottomBarDesign.Sizes.PillHeight)
                        .background(
                            BottomBarDesign.Colors.indicatorColor(),
                            BottomBarDesign.Shapes.indicatorShape
                        )
                )

                Row(
                    modifier = Modifier
                        .matchParentSize()
                        .onSizeChanged { contentWidthPx = it.width },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                        items.forEachIndexed { index, item ->
                            val isSelected = index == selectedIndex
                            val iconColor =
                                if (isSelected) BottomBarDesign.Colors.selectedIconColor()
                                else BottomBarDesign.Colors.unselectedIconColor()

                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                IconButton(
                                    onClick = {
                                        nav.navigate(item.route) {
                                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    modifier = Modifier.size(BottomBarDesign.Sizes.IconButton)
                                ) { Icon(item.icon, null, tint = iconColor) }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(bottomPad))
        }
    }
}
