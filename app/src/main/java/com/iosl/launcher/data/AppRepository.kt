package com.iosl.launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.provider.MediaStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

class AppRepository(context: Context) {
    private val app = context.applicationContext
    private val launcherApps = app.getSystemService(LauncherApps::class.java)
    private val pm = app.packageManager

    fun loadApps(): List<AppItem> =
        launcherApps.profiles
            .flatMap { user -> launcherApps.getActivityList(null, user) }
            .map(::AppItem)
            .sortedBy { it.label.lowercase() }

    /** 앱 설치/삭제/변경 시 신호를 방출한다. */
    fun appChanges(): Flow<Unit> = callbackFlow {
        val cb = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String?, user: UserHandle?) { trySend(Unit) }
            override fun onPackageAdded(packageName: String?, user: UserHandle?) { trySend(Unit) }
            override fun onPackageChanged(packageName: String?, user: UserHandle?) { trySend(Unit) }
            override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) { trySend(Unit) }
            override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) { trySend(Unit) }
        }
        launcherApps.registerCallback(cb, Handler(Looper.getMainLooper()))
        awaitClose { launcherApps.unregisterCallback(cb) }
    }.conflate()

    /** iOS Dock 기본 구성: 전화 / 메시지 / 브라우저 / 카메라 */
    fun defaultDockPackages(): List<String> {
        val intents = listOf(
            Intent(Intent.ACTION_DIAL),
            Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")),
            Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER),
            Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
        )
        return intents
            .mapNotNull { pm.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName }
            .filter { it != "android" }
            .distinct()
    }
}
