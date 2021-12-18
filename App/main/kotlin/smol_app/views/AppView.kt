import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.Children
import smol_access.SL
import smol_app.*
import smol_app.browser.DownloadItem
import smol_app.browser.ModBrowserView
import smol_app.browser.downloadCard
import smol_app.home.homeView
import smol_app.navigation.Screen
import smol_app.themes.SmolTheme
import smol_app.themes.SmolTheme.toColors
import smol_app.views.FileDropper
import smol_app.views.ProfilesView
import smol_app.views.settingsView

@OptIn(ExperimentalStdlibApi::class)
@Composable
@Preview
fun AppState.appView() {
    val theme = SL.themeManager.activeTheme.collectAsState()
    val toasterState = remember { ToasterState() }

    MaterialTheme(
        colors = theme.value.second.toColors(),
        typography = Typography(
            button = TextStyle(fontFamily = SmolTheme.orbitronSpaceFont)
        )
    ) {
        Children(router.state) { screen ->
            Box {
                when (screen.configuration) {
                    is Screen.Home -> homeView()
                    is Screen.Settings -> settingsView()
                    is Screen.Profiles -> ProfilesView()
                    is Screen.ModBrowser -> ModBrowserView()
                }.run { }
                FileDropper()

                // Downloads
                val downloads = SL.UI.downloadManager.downloads.collectAsState()
                toaster(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                    toasterState = toasterState,
                    items = downloads.value.map {
                        Toast(id = it.id, timeoutMillis = null) { downloadCard(download = it) }
                    },
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                )

                downloads.value
                    .filter { it.status.value is DownloadItem.Status.Completed }
                    .forEach {
                        if (!toasterState.timersByToastId.containsKey(it.id)) {
                            toasterState.timersByToastId[it.id] = Toaster.defaultTimeoutMillis
                        }
                    }
            }
        }
    }
}