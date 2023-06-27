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

package smol.access.config

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import smol.access.Constants
import smol.timber.ktx.Timber
import smol.utilities.*
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.isWritable
import kotlin.io.path.pathString


class GamePathManager internal constructor(
    private val appConfig: AppConfig
) {
    private val value_ = MutableStateFlow(appConfig.gamePath.toPathOrNull())

    val path = value_.asStateFlow()
        .apply {
            CoroutineScope(Job()).launch {
                this@apply.collect {
                    appConfig.gamePath = it?.pathString
                }
            }
        }

    fun set(path: String) = path.toPathOrNull()?.run { set(this) }
    fun set(path: Path) = value_.update { path }

    fun getDefaultStarsectorPath(platform: Platform): File? =
        kotlin.runCatching {
            when (platform) {
                Platform.Windows ->
                    Advapi32Util.registryGetStringValue(
                        WinReg.HKEY_CURRENT_USER,
                        "SOFTWARE\\Fractal Softworks\\Starsector",
                        ""
                    )
                Platform.MacOS -> "/Applications/Starsector.app"
                Platform.Linux -> "" // TODO
                else -> "" // TODO
            }
        }
            .mapCatching { File(it) }
            .onFailure {
                Timber.d { it.message ?: "" }
                it.printStackTrace()
            }
            .getOrNull()

    fun getGameExeFolderPath(gameFolderPath: Path = value_.value!!) =
        when(currentPlatform) {
            Platform.Windows -> gameFolderPath
            Platform.MacOS -> gameFolderPath.parent
            Platform.Linux -> TODO()
            else -> null
        }

    fun getGameCoreFolderPath(gameFolderPath: Path = value_.value!!) =
        when(currentPlatform) {
            Platform.Windows -> gameFolderPath
            Platform.MacOS -> gameFolderPath.resolve("Contents/Resources/Java")
            Platform.Linux -> TODO()
            else -> null
        }

    fun getModsPath(): Path? {
        val starsectorPath = path.value?.run { if (!this.exists()) null else this }
            ?: kotlin.run {
                val ex = NullPointerException("Game path not found. AppConfig: $appConfig")
                Timber.e(ex)
                return null
            }

        val mods = starsectorPath.resolve(Constants.MODS_FOLDER_NAME)

        IOLock.write {
            if (!mods.exists()) {
                if (!mods.isWritable()) {
                    Timber.e { "Unable to write to ${mods.absolutePathString()}. Ensure that it exists and SMOL has write permission (run as admin?)." }
                    return null
                } else {
                    mods.createDirectories()
                }
            }
        }

        return mods
    }
}