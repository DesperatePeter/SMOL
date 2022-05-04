/*
 * This file is distributed under the GPLv3. An informal description follows:
 * - Anyone can copy, modify and distribute this software as long as the other points are followed.
 * - You must include the license and copyright notice with each and every distribution.
 * - You may this software for commercial purposes.
 * - If you modify it, you must indicate changes made to the code.
 * - Any modifications of this code base MUST be distributed with the same license, GPLv3.
 * - This software is provided without warranty.
 * - The software author or license can not be held liable for any damages inflicted by the software.
 * The full license is available from <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

@file:OptIn(ExperimentalFoundationApi::class)

package smol.app.home

import AppScope
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.replaceCurrent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import smol.access.SL
import smol.access.model.Mod
import smol.app.UI
import smol.app.cli.SmolCLI
import smol.app.composables.*
import smol.app.navigation.Screen
import smol.app.themes.SmolTheme
import smol.app.toolbar.toolbar
import smol.app.util.filterModGrid
import smol.app.util.onEnterKeyPressed
import smol.app.util.replaceAllUsingDifference
import smol.utilities.IOLock


@OptIn(
    ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
@Preview
fun AppScope.homeView(
    modifier: Modifier = Modifier
) {
    val mods: SnapshotStateList<Mod> = remember { mutableStateListOf() }
    val shownMods: SnapshotStateList<Mod?> = mods.toMutableStateList()
    val isWriteLocked = IOLock.stateFlow.collectAsState()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            SL.access.mods.collectLatest { freshMods ->
                if (freshMods != null) {
                    withContext(Dispatchers.Main) {
                        mods.replaceAllUsingDifference(freshMods.mods, doesOrderMatter = true)
                    }
                }
            }
        }
    }

//    var showConfirmMigrateDialog: Boolean by remember { mutableStateOf(false) }
    val showLogPanel = remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        topBar = {
            SmolTopAppBar(modifier = Modifier.height(SmolTheme.topBarHeight)) {
                toolbar(router.state.value.activeChild.instance as Screen)

                if (isWriteLocked.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    SmolText(
                        text = SL.access.modModificationState.collectAsState().value
                            .firstNotNullOfOrNull { it.value != smol.access.Access.ModModificationState.Ready }
                            ?.toString() ?: "",
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val scope = rememberCoroutineScope()
                    smolSearchField(
                        modifier = Modifier
                            .focusRequester(searchFocusRequester())
                            .widthIn(min = 100.dp, max = 300.dp)
                            .padding(end = 16.dp)
                            .offset(y = (-3).dp)
                            .align(Alignment.CenterVertically),
                        tooltipText = "Hotkey: Ctrl-F",
                        label = "Filter"
                    ) { query ->
                        if (query.isBlank()) {
                            shownMods.replaceAllUsingDifference(mods, doesOrderMatter = false)
                        } else {
                            scope.launch {
                                val newModGrid = filterModGrid(query, mods, access = SL.access).ifEmpty { listOf(null) }

                                withContext(Dispatchers.Main) {
                                    shownMods.replaceAllUsingDifference(
                                        newModGrid,
                                        doesOrderMatter = true
                                    )
                                }
                            }
                        }
                    }

                    // Hide console for now, it's not useful
                    if (false) {
                        consoleTextField(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .padding(end = 16.dp)
                                .offset(y = (-3).dp)
                                .align(Alignment.CenterVertically)
                        )
                    }

                    SmolTooltipArea(tooltip = { SmolTooltipText("About") }) {
                        IconButton(
                            onClick = { router.replaceCurrent(Screen.About) },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                painter = painterResource("icon-info.svg"),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp).align(Alignment.CenterVertically),
                            )
                        }
                    }
                }
            }
        }, content = {
            Box {
                val validationResult = SL.access.validatePaths()

                if (validationResult.isSuccess) {
                    ModGridView(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(bottom = 40.dp),
                        mods = (if (shownMods.isEmpty()) mods else shownMods) as SnapshotStateList<Mod?>
                    )
                } else {
                    Column(
                        Modifier.fillMaxWidth().fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val errors = validationResult.failure?.flatMap { it.value }

                        if (errors?.any() == true) {
                            Text(text = errors.joinToString(separator = "\n\n") { "Error: $it" })
                        }
                        SmolButton(
                            onClick = { router.replaceCurrent(Screen.Settings) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Settings")
                        }
                    }
                }
            }

            if (showLogPanel.value) {
                logPanel { showLogPanel.value = false }
            }
        },
        bottomBar = {
            SmolBottomAppBar(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    logButtonAndErrorDisplay(showLogPanel = showLogPanel)
                }
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AppScope.consoleTextField(
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Row {
            var consoleText by remember { mutableStateOf("") }
            SmolOutlinedTextField(
                value = consoleText,
                label = { Text("Console") },
                maxLines = 1,
                singleLine = true,
                onValueChange = { newStr ->
                    consoleText = newStr
                },
                leadingIcon = { Icon(painter = painterResource("icon-console.svg"), contentDescription = null) },
                modifier = Modifier
                    .onEnterKeyPressed {
                        kotlin.runCatching {
                            SmolCLI(
                                userManager = SL.userManager,
                                userModProfileManager = SL.userModProfileManager,
                                vmParamsManager = SL.UI.vmParamsManager,
                                access = SL.access,
                                gamePathManager = SL.gamePathManager
                            )
                                .parse(consoleText)
                            consoleText = ""
                        }
                            .onFailure { Logger.warn(it) }
                        true
                    }
            )
        }
    }
}