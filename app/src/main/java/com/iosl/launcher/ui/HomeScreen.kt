package com.iosl.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iosl.launcher.COLUMNS
import com.iosl.launcher.HomeState
import com.iosl.launcher.ROWS
import com.iosl.launcher.data.AppItem
import com.iosl.launcher.motion.IosMotion
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

typealias LaunchCallback = (AppItem, android.graphics.Rect) -> Unit

@Composable
fun HomeScreen(state: HomeState, onLaunch: LaunchCallback) {
    // 정적인 월페이퍼만 백드롭으로 캡처한다. (아이콘/페이저는 캡처하지 않음 → 스와이프 중 재기록 비용 0)
    val backdrop = rememberLayerBackdrop()

    Box(Modifier.fillMaxSize()) {
        Wallpaper(Modifier.fillMaxSize().layerBackdrop(backdrop))

        Column(Modifier.fillMaxSize()) {
            val pager = rememberPagerState { state.pages.size.coerceAtLeast(1) }

            HorizontalPager(
                state = pager,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars),
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pager,
                    snapAnimationSpec = IosMotion.smooth(),
                ),
                key = { it },
            ) { page ->
                AppGrid(state.pages.getOrNull(page).orEmpty(), onLaunch)
            }

            PageDots(pager)
            Dock(state.dock, backdrop, onLaunch)
        }
    }
}

@Composable
private fun AppGrid(apps: List<AppItem>, onLaunch: LaunchCallback) {
    Column(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 8.dp)) {
        repeat(ROWS) { r ->
            Row(Modifier.weight(1f).fillMaxWidth()) {
                repeat(COLUMNS) { c ->
                    Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        apps.getOrNull(r * COLUMNS + c)?.let { AppIcon(it, onLaunch) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PageDots(pager: PagerState) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(pager.pageCount) { i ->
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .size(7.dp)
                    .alpha(if (i == pager.currentPage) 1f else 0.4f)
                    .background(Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun Dock(apps: List<AppItem>, backdrop: Backdrop, onLaunch: LaunchCallback) {
    Row(
        Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(36.dp) },
                effects = {
                    vibrancy()
                    blur(2f.dp.toPx())
                    lens(12f.dp.toPx(), 24f.dp.toPx())
                },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.08f)) },
            )
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        apps.forEach { AppIcon(it, onLaunch, showLabel = false) }
    }
}
