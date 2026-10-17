package com.iosl.launcher.data

import android.content.pm.LauncherActivityInfo

/** key 로만 동등성을 판단한다. (갱신 시 LauncherActivityInfo 인스턴스가 바뀌어도 불필요한 리컴포지션 방지) */
class AppItem(val info: LauncherActivityInfo) {
    val key: String = "${info.componentName.flattenToString()}#${info.user.hashCode()}"
    val label: String = info.label.toString()
    val packageName: String get() = info.componentName.packageName

    override fun equals(other: Any?) = other is AppItem && other.key == key
    override fun hashCode() = key.hashCode()
}
