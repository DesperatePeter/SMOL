package smol_app.modprofiles

import AppState
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import smol_access.SL
import smol_access.model.ModVariant
import smol_access.model.UserProfile
import smol_app.composables.*
import smol_app.themes.SmolTheme
import smol_app.toolbar.*

@OptIn(
    ExperimentalMaterialApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
@Preview
fun AppState.ProfilesView(
    modifier: Modifier = Modifier
) {
    val modProfileIdShowingDeleteConfirmation = remember { mutableStateOf<Int?>(null) }
    var userProfile = SL.userManager.activeProfile.collectAsState().value
    val saveGames = SL.saveReader.saves.collectAsState()
    val showLogPanel = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(modifier = Modifier.height(SmolTheme.topBarHeight)) {
                launchButton()
                installModsButton()
                Spacer(Modifier.width(16.dp))
                homeButton()
                screenTitle(text = "Mod Profiles")
                settingsButton()
                modBrowserButton()
            }
        }, content = {
            Box(modifier.padding(16.dp)) {
                val modVariants = remember {
                    mutableStateOf(SL.access.mods.value?.mods?.flatMap { it.variants }?.associateBy { it.smolId }
                        ?: emptyMap())
                }

                LazyVerticalGrid(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    cells = GridCells.Adaptive(370.dp)
                ) {
                    this.items(items = userProfile.modProfiles + saveGames.value.mapIndexed { index, saveFile ->
                        UserProfile.ModProfile(
                            id = 1337 + index,
                            name = saveFile.characterName,
                            description = "",
                            sortOrder = 1337 + index,
                            enabledModVariants = saveFile.mods.map {
                                UserProfile.ModProfile.EnabledModVariant(
                                    modId = it.id,
                                    smolVariantId = ModVariant.createSmolId(it.id, it.version)
                                )
                            }
                        )
                    }
                        .sortedWith(
                            compareByDescending<UserProfile.ModProfile> { it.id == userProfile.activeModProfileId }
                                .thenBy { it.sortOrder })
                    ) { modProfile ->
                        ModProfileCard(userProfile, modProfile, modProfileIdShowingDeleteConfirmation, modVariants)
                    }

                    this.item {
                        var newProfileName by remember { mutableStateOf("") }
                        Card(
                            shape = SmolTheme.smolFullyClippedButtonShape()
                        ) {
                            Column(Modifier.padding(16.dp).fillMaxWidth()) {
                                SmolTextField(
                                    value = newProfileName,
                                    onValueChange = { newProfileName = it },
                                    singleLine = true,
                                    label = { Text("Name") }
                                )
                                SmolButton(
                                    modifier = Modifier.padding(top = 16.dp),
                                    onClick = {
                                        if (newProfileName.isNotBlank()) {
                                            SL.userManager.createModProfile(
                                                name = newProfileName,
                                                description = null,
                                                sortOrder = SL.userManager.activeProfile.value.modProfiles.maxOf { it.sortOrder } + 1
                                            )
                                            newProfileName = ""
                                            userProfile = SL.userManager.activeProfile.value
                                        }
                                    }) {
                                    Icon(
                                        modifier = Modifier
                                            .height(SmolTheme.textIconHeightWidth())
                                            .width(SmolTheme.textIconHeightWidth()),
                                        painter = painterResource("icon-plus.svg"),
                                        contentDescription = null
                                    )
                                    Text(
                                        text = "New Profile"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showLogPanel.value) {
                logPanel { showLogPanel.value = false }
            }
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth()
            ) {
                logButtonAndErrorDisplay(showLogPanel = showLogPanel)
            }
        }
    )

    if (modProfileIdShowingDeleteConfirmation.value != null) {
        val profile =
            userProfile.modProfiles.firstOrNull { it.id == modProfileIdShowingDeleteConfirmation.value }
        SmolAlertDialog(
            modifier = Modifier,
            onDismissRequest = { modProfileIdShowingDeleteConfirmation.value = null },
            title = { Text("Confirm deletion", style = SmolTheme.alertDialogTitle()) },
            text = {
                Text("Are you sure you want to delete \"${profile?.name}\"?", style = SmolTheme.alertDialogBody())
            },
            confirmButton = {
                SmolButton(onClick = {
                    SL.userManager.removeModProfile(
                        modProfileIdShowingDeleteConfirmation.value ?: run {
                            modProfileIdShowingDeleteConfirmation.value = null

                            return@SmolButton
                        })
                    modProfileIdShowingDeleteConfirmation.value = null
                }) { Text("Delete") }
            },
            dismissButton = {
                SmolSecondaryButton(onClick = { modProfileIdShowingDeleteConfirmation.value = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}