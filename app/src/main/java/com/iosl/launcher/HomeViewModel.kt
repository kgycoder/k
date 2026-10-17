package com.iosl.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iosl.launcher.data.AppItem
import com.iosl.launcher.data.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HomeState(
    val pages: List<List<AppItem>> = emptyList(),
    val dock: List<AppItem> = emptyList(),
)

/** iPhone 홈 화면: 4열 x 6행 */
const val COLUMNS = 4
const val ROWS = 6

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(app)
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {
        viewModelScope.launch {
            repo.appChanges().onStart { emit(Unit) }.collectLatest {
                _state.value = withContext(Dispatchers.Default) { build() }
            }
        }
    }

    private fun build(): HomeState {
        val apps = repo.loadApps()
        val dockPkgs = repo.defaultDockPackages()
        val dock = dockPkgs.mapNotNull { p -> apps.firstOrNull { it.packageName == p } }
        val rest = apps - dock.toSet()
        return HomeState(pages = rest.chunked(COLUMNS * ROWS), dock = dock)
    }
}
