package com.iosl.launcher.ui

import android.app.WallpaperManager
import android.content.res.Resources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

private sealed interface WallpaperState {
    data object Loading : WallpaperState
    data object Unavailable : WallpaperState
    class Ready(val bitmap: ImageBitmap) : WallpaperState
}

/**
 * Liquid Glass 가 굴절시킬 대상은 Compose 레이어 안에 있어야 하므로
 * 월페이퍼를 Compose 로 직접 그린다. (정적 콘텐츠 → 백드롭 레이어는 한 번만 기록됨)
 */
@Composable
fun Wallpaper(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val state by produceState<WallpaperState>(WallpaperState.Loading) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val d = WallpaperManager.getInstance(context).drawable ?: return@runCatching null
                val dm = Resources.getSystem().displayMetrics
                val maxSide = max(dm.widthPixels, dm.heightPixels) * 1.15f
                val iw = d.intrinsicWidth.coerceAtLeast(1)
                val ih = d.intrinsicHeight.coerceAtLeast(1)
                val s = min(1f, maxSide / max(iw, ih))
                d.toBitmap((iw * s).toInt(), (ih * s).toInt()).asImageBitmap()
            }.getOrNull()?.let { WallpaperState.Ready(it) } ?: WallpaperState.Unavailable
        }
    }

    Box(modifier) {
        when (val s = state) {
            WallpaperState.Loading -> Unit // 시스템 월페이퍼(windowShowWallpaper)가 비쳐 보인다
            WallpaperState.Unavailable -> Box(
                Modifier.fillMaxSize().background(
                    Brush.linearGradient(listOf(Color(0xFF3A6BFF), Color(0xFF8E4DFF), Color(0xFFFF6FA5)))
                )
            )
            is WallpaperState.Ready -> Image(
                bitmap = s.bitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
