package smol_access.business

import smol_access.model.Dependency
import smol_access.model.Mod
import smol_access.model.ModVariant
import timber.ktx.Timber


fun ModVariant.findDependencies(mods: List<Mod>): List<Pair<Dependency, Mod?>> =
    this.modInfo.dependencies
        .map { dep -> dep to mods.firstOrNull { it.id == dep.id } }

fun ModVariant.findDependencyStates(mods: List<Mod>): List<DependencyState> =
    (mod.findFirstEnabled?.findDependencies(mods) ?: emptyList())
        .map { (dependency, foundDependencyMod) ->
            // Mod not found or has no variants, it's missing.
            if (foundDependencyMod == null || foundDependencyMod.variants.isEmpty())
                return@map DependencyState.Missing(
                    dependency = dependency,
                    outdatedModIfFound = null
                )

            return@map if (dependency.version == null) {
                if (foundDependencyMod.hasEnabledVariant) {
                    // No specific version needed, one is enabled, all good.
                    DependencyState.Enabled(dependency, foundDependencyMod.findFirstEnabled!!)
                } else {
                    // No specific version needed, none are enabled but one exists, return highest version available.
                    DependencyState.Disabled(dependency, foundDependencyMod.findHighestVersion!!)
                }
            } else {
                val variantsMeetingVersionReq =
                    foundDependencyMod.variants.filter { it.modInfo.version >= dependency.version!! }

                if (variantsMeetingVersionReq.isEmpty()) {
                    // No variants found will work.
                    DependencyState.Missing(dependency, foundDependencyMod)
                } else {
                    val alreadyEnabledAndValidDependency = variantsMeetingVersionReq
                        .filter { foundDependencyMod.isEnabled(it) }
                        .maxByOrNull { it.modInfo.version }

                    if (alreadyEnabledAndValidDependency != null) {
                        DependencyState.Enabled(dependency, alreadyEnabledAndValidDependency)
                    } else {
                        val validButDisabledDependency = variantsMeetingVersionReq
                            .maxByOrNull { it.modInfo.version }
                        if (validButDisabledDependency == null) {
                            Timber.w { "Unexpected scenario finding dependency for mod ${mod.id} with dependency: $dependency." }
                            DependencyState.Missing(dependency, foundDependencyMod)
                        } else {
                            DependencyState.Disabled(dependency, validButDisabledDependency)
                        }
                    }
                }
            }
        }
        .onEach {
            when (it) {
                is DependencyState.Disabled -> Timber.d { "Dependency disabled: $it" }
                is DependencyState.Enabled -> Timber.v { "Dependency enabled: $it" }
                is DependencyState.Missing -> Timber.d { "Dependency missing: $it" }
            }
        }

sealed class DependencyState {
    abstract val dependency: Dependency

    data class Missing(override val dependency: Dependency, val outdatedModIfFound: Mod?) : DependencyState()
    data class Disabled(override val dependency: Dependency, val variant: ModVariant) : DependencyState()
    data class Enabled(override val dependency: Dependency, val variant: ModVariant) : DependencyState()
}