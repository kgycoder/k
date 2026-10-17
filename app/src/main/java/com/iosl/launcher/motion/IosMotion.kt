package com.iosl.launcher.motion

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import kotlin.math.PI

/**
 * SwiftUI 의 spring(response:dampingFraction:) 을 Compose spring 으로 변환한다.
 * mass = 1 이므로 stiffness = (2π / response)^2.
 */
fun <T> iosSpring(response: Float = 0.5f, dampingFraction: Float = 0.825f): SpringSpec<T> {
    val omega = (2.0 * PI / response).toFloat()
    return spring(dampingRatio = dampingFraction, stiffness = omega * omega)
}

object IosMotion {
    /** .smooth — 바운스 없음 */
    fun <T> smooth(): SpringSpec<T> = iosSpring(0.5f, 1.0f)
    /** .snappy — 약간 빠르고 미세한 바운스 */
    fun <T> snappy(): SpringSpec<T> = iosSpring(0.4f, 0.85f)
    /** .bouncy */
    fun <T> bouncy(): SpringSpec<T> = iosSpring(0.5f, 0.7f)
}
