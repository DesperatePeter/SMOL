package smol_access.config

import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.ktx.Timber
import utilities.IOLock
import utilities.Platform
import utilities.toPathOrNull
import java.io.File
import java.nio.file.Path
import kotlin.io.path.*


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
                Platform.MacOS -> "" // TODO
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

    fun getModsPath(): Path? {
        val starsectorPath = path.value?.run { if (!this.exists()) null else this }
            ?: kotlin.run {
                val ex = NullPointerException("Game path not found. AppConfig: $appConfig")
                Timber.e(ex)
                return null
            }

        val mods = starsectorPath.resolve("mods")

        IOLock.write {
            if (!mods.isWritable()) {
                Timber.e { "Unable to write to ${mods.absolutePathString()}. Ensure that it exists and SMOL has write permission (run as admin?)." }
                return null
            } else {
                mods.createDirectories()
            }
        }

        return mods
    }
}