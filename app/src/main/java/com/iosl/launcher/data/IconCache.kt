package com.iosl.launcher.data

import android.graphics.AdaptiveIconDrawable
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 아이콘을 화면에 보일 때만(Lazy) 디코딩하고 LRU 로 캐시한다.
 * Adaptive 아이콘은 기기 마스크(원형 등)를 적용하지 않고 full-bleed 로 그려
 * iOS 의 스퀴클 마스크를 Compose 쪽에서 씌울 수 있게 한다.
 */
object IconCache {
    private val cache = LruCache<String, ImageBitmap>(192)

    fun peek(item: AppItem): ImageBitmap? = cache.get(item.key)

    suspend fun load(item: AppItem, sizePx: Int): ImageBitmap =
        cache.get(item.key) ?: withContext(Dispatchers.Default) {
            val drawable = item.info.getIcon(0)
            render(drawable, sizePx).asImageBitmap().also { cache.put(item.key, it) }
        }

    private fun render(d: Drawable, s: Int): Bitmap {
        val bmp = createBitmap(s, s)
        val c = Canvas(bmp)
        if (d is AdaptiveIconDrawable) {
            // 108dp 캔버스의 중앙 72dp(safe zone)가 s 를 채우도록 1.5배 확대
            val o = (s * 0.25f).toInt()
            d.background?.apply { setBounds(-o, -o, s + o, s + o); draw(c) }
            d.foreground?.apply { setBounds(-o, -o, s + o, s + o); draw(c) }
        } else {
            d.setBounds(0, 0, s, s)
            d.draw(c)
        }
        return bmp
    }
}
