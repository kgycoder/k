package com.iosl.launcher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iosl.launcher.data.AppItem
import com.iosl.launcher.data.IconCache
import com.iosl.launcher.motion.iosSpring
import kotlinx.coroutines.launch

private val IconSize = 60.dp
private val IconShape = RoundedCornerShape(percent = 22) // iOS 스퀴클 근사. 추후 연속 곡률 셰이프로 교체

private class BoundsHolder { var rect = android.graphics.Rect() }

@Composable
fun AppIcon(
    item: AppItem,
    onLaunch: (AppItem, android.graphics.Rect) -> Unit,
    showLabel: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val sizePx = with(LocalDensity.current) { IconSize.roundToPx() }
    // 화면에 컴포지션될 때만 디코딩 (Lazy). 캐시 히트 시 즉시 표시.
    val bitmap: ImageBitmap? by produceState(IconCache.peek(item), item.key) {
        if (value == null) value = IconCache.load(item, sizePx)
    }

    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val holder = remember { BoundsHolder() }

    Column(
        modifier
            .width(76.dp)
            .onGloballyPositioned {
                val r = it.boundsInWindow()
                holder.rect.set(r.left.toInt(), r.top.toInt(), r.right.toInt(), r.bottom.toInt())
            }
            // 값 읽기를 graphicsLayer(draw 단계)로 한정 → 리컴포지션 없이 애니메이션
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .pointerInput(item.key) {
                detectTapGestures(
                    onPress = {
                        scope.launch { scale.animateTo(0.9f, iosSpring(0.25f, 0.8f)) }
                        tryAwaitRelease()
                        scope.launch { scale.animateTo(1f, iosSpring(0.4f, 0.6f)) }
                    },
                    onTap = { onLaunch(item, holder.rect) },
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val b = bitmap
        if (b != null) {
            Image(b, contentDescription = item.label, modifier = Modifier.size(IconSize).clip(IconShape))
        } else {
            Spacer(Modifier.size(IconSize))
        }
        if (showLabel) {
            Spacer(Modifier.height(5.dp))
            BasicText(
                text = item.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(Color.Black.copy(alpha = 0.35f), Offset(0f, 1f), 3f),
                ),
            )
        }
    }
}
