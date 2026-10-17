package com.iosl.launcher

import android.app.ActivityOptions
import android.content.pm.LauncherApps
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.iosl.launcher.data.AppItem
import com.iosl.launcher.ui.HomeScreen

class LauncherActivity : ComponentActivity() {
    private val vm: HomeViewModel by viewModels()
    private val launcherApps by lazy { getSystemService(LauncherApps::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            val state by vm.state.collectAsState()
            BackHandler(true) { /* 홈 런처는 뒤로가기로 종료되지 않는다 */ }
            HomeScreen(state = state, onLaunch = ::launch)
        }
    }

    private fun launch(item: AppItem, bounds: Rect) {
        val opts = ActivityOptions.makeScaleUpAnimation(
            window.decorView, bounds.left, bounds.top, bounds.width(), bounds.height(),
        )
        launcherApps.startMainActivity(item.info.componentName, item.info.user, bounds, opts.toBundle())
    }
}
