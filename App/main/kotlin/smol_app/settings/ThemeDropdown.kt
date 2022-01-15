package smol_app.settings

import AppState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.mouseClickable
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import smol_app.composables.SmolDropdownMenuItemCustom
import smol_app.composables.SmolDropdownWithButton
import smol_app.composables.SmolLinkText
import smol_app.themes.SmolTheme.toColors
import smol_app.util.openInDesktop

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppState.themeDropdown(modifier: Modifier = Modifier): String {
    var themeName by remember { mutableStateOf(smol_access.SL.themeManager.activeTheme.value.first) }
    val themes = smol_access.SL.themeManager.getThemes()
    val recomposeScope = currentRecomposeScope

    Column(modifier) {
        Text(text = "Theme", style = SettingsView.settingLabelStyle())
        Row {
            SmolDropdownWithButton(
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                items = themes
                    .map { entry ->
                        val colors = entry.value.toColors()
                        SmolDropdownMenuItemCustom(
                            backgroundColor = colors.surface,
                            onClick = {
                                themeName = entry.key
                                smol_access.SL.themeManager.setActiveTheme(entry.key)
                            },
                            customItemContent = { isMenuButton ->
                                val height = 24.dp
                                Text(
                                    text = entry.key,
                                    modifier = Modifier
                                        .run { if (!isMenuButton) this.weight(1f) else this }
                                        .align(Alignment.CenterVertically),
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = colors.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .width(height * 3)
                                        .height(height)
                                        .background(color = colors.primary)
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .width(height)
                                        .height(height)
                                        .background(color = colors.secondary)
                                )
                            }
                        )
                    },
                initiallySelectedIndex = themes.keys.indexOf(themeName).coerceAtLeast(0),
                canSelectItems = true
            )
            SmolLinkText(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
                    .mouseClickable { smol_access.Constants.THEME_CONFIG_PATH.openInDesktop() }, text = "Edit"
            )
            SmolLinkText(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .align(Alignment.CenterVertically)
                    .mouseClickable {
                        smol_access.SL.themeManager.reloadThemes()
                        recomposeScope.invalidate()
                    },
                text = "Refresh"
            )
        }
    }

    return themeName
}