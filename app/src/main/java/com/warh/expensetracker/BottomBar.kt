package com.warh.expensetracker

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.warh.commons.scroll_utils.collapseByPx
import com.warh.designsystem.bottom_bar.BottomBarDesign
import kotlin.math.roundToInt

data class BarItem(val route: String, val icon: ImageVector)

@Composable
fun BottomBar(
    nav: NavHostController,
    currentRoute: String?,
    offsetY: Float,
    onMeasuredHeight: (Int) -> Unit,
    isSettling: Boolean, // <- de rememberHideOnScrollState()
) {
    val navInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)

    val items = remember {
        listOf(
            BarItem(Destinations.TRANSACTIONS, Icons.Default.History),
            BarItem(Destinations.BUDGETS,      Icons.Default.PieChart),
            BarItem(Destinations.CATEGORIES,   Icons.Default.Category),
            BarItem(Destinations.ACCOUNTS,     Icons.Default.AccountBalance),
        )
    }
    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    val density = LocalDensity.current
    val rowHeightPx = with(density) {
        (BottomBarDesign.Sizes.RowHeight + BottomBarDesign.Sizes.PadTop + BottomBarDesign.Sizes.PadBottom).roundToPx()
    }
    val navBottomPx = navInsets.getBottom(density)

    // factor 0..1 de colapso
    val maxCollapsePx = rowHeightPx + navBottomPx
    val t = (offsetY / maxCollapsePx).coerceIn(0f, 1f)

    // padding rojo que se reparte arriba/abajo
    val topPadPx    = (navBottomPx * t).roundToInt()
    val bottomPadPx = navBottomPx - topPadPx
    val topPad: Dp    = with(density) { topPadPx.toDp() }
    val bottomPad: Dp = with(density) { bottomPadPx.toDp() }

    // si hay settle (fling), suavizamos el recorte
    val cutTarget = offsetY.roundToInt()
    val cutPx by if (isSettling)
        animateIntAsState(
            targetValue = cutTarget,
            animationSpec = tween(
                durationMillis = BottomBarDesign.Anim.SLIDE_MS,
                easing = BottomBarDesign.Anim.slideEasing
            ),
            label = "barCut"
        )
    else
        rememberUpdatedState(cutTarget)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BottomBarDesign.Colors.containerColor())
            .clipToBounds()
            .collapseByPx(cutPx, minHeightPx = navBottomPx)
            .onSizeChanged { onMeasuredHeight(rowHeightPx + navBottomPx) }
    ) {
        Column(Modifier.fillMaxWidth()) {
            // padding superior (rojo puro) que crece con el colapso
            Spacer(Modifier.height(topPad))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BottomBarDesign.Sizes.RowHeight)
                    .padding(
                        PaddingValues(
                            start  = BottomBarDesign.Sizes.PadStartEnd,
                            end    = BottomBarDesign.Sizes.PadStartEnd,
                            top    = BottomBarDesign.Sizes.PadTop,
                            bottom = BottomBarDesign.Sizes.PadBottom
                        )
                    )
            ) {
                var contentWidthPx by remember { mutableIntStateOf(0) }
                val pillWidthPx = with(density) { BottomBarDesign.Sizes.PillWidth.roundToPx() }

                // --- ANIMACIÓN DEL INDICADOR (pill) MEJORADA ---
                val animX = rememberPillXAnimator(
                    selectedIndex = selectedIndex,
                    slots = items.size,
                    contentWidthPx = contentWidthPx,
                    pillWidthPx = pillWidthPx
                )

                // feedback sutil mientras se mueve
                val moving = remember { mutableStateOf(false) }
                LaunchedEffect(animX) {
                    moving.value = true
                    // micro ventana de tiempo; al llegar a target vuelve a 1f
                    moving.value = false
                }
                val scaleX by animateFloatAsState(
                    targetValue = if (moving.value) 1.06f else 1f,
                    animationSpec = tween(160),
                    label = "pillScaleX"
                )
                val alpha  by animateFloatAsState(
                    targetValue = if (moving.value) 0.96f else 1f,
                    animationSpec = tween(160),
                    label = "pillAlpha"
                )

                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .offset { IntOffset(animX, 0) }
                        .size(BottomBarDesign.Sizes.PillWidth, BottomBarDesign.Sizes.PillHeight)
                        .graphicsLayer {
                            this.scaleX = scaleX
                            this.alpha = alpha
                        }
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

            // padding inferior (rojo puro) que decrece con el colapso
            Spacer(Modifier.height(bottomPad))
        }
    }
}

/** Calcula y anima el X del indicador “pill”. */
@Composable
private fun rememberPillXAnimator(
    selectedIndex: Int,
    slots: Int,
    contentWidthPx: Int,
    pillWidthPx: Int
): Int {
    fun targetFor(index: Int): Int {
        if (contentWidthPx <= 0 || slots == 0) return 0
        val slotW = contentWidthPx / slots
        return (slotW * index) + (slotW - pillWidthPx) / 2
    }
    val targetX = targetFor(selectedIndex)

    val animX by animateIntAsState(
        targetValue = targetX,
        animationSpec = tween(
            durationMillis = BottomBarDesign.Anim.SLIDE_MS,
            easing = BottomBarDesign.Anim.slideEasing
        ),
        label = "pillX"
    )
    return animX
}