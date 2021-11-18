package util

import FORUM_PAGE_URL
import ServiceLocator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import business.VmParamsManager
import config.Platform
import model.Mod
import java.awt.Desktop
import java.net.URI
import kotlin.math.ceil


fun Mod.getModThreadId(): ModThreadId? =
    findFirstEnabled?.versionCheckerInfo?.modThreadId
        ?: findHighestVersion?.versionCheckerInfo?.modThreadId

fun ModThreadId.openModThread() {
    (FORUM_PAGE_URL + this).openAsUriInBrowser()
}

fun String.openAsUriInBrowser() {
    Desktop.getDesktop().browse(URI(this))
}

typealias ModThreadId = String

/**
 * Return a string with a maximum length of `length` characters.
 * If there are more than `length` characters, then string ends with an ellipsis ("...").
 *
 * @param text
 * @param length
 * @return
 */
fun String.ellipsizeAfter(length: Int): String? {
    // The letters [iIl1] are slim enough to only count as half a character.
    var lengthMod = length
    lengthMod += ceil(this.replace("[^iIl]".toRegex(), "").length / 2.0).toInt()
    return if (this.length > lengthMod) {
        this.substring(0, lengthMod - 3) + "…"
    } else this
}

/**
 * A mebibyte is 2^20 bytes (1024 KiB instead of 1000 KB).
 */
val Long.bytesAsReadableMiB: String
    get() = "%.3f MiB".format(this / 1048576f)

/**
 * From [https://github.com/JetBrains/skija/blob/ebd63708b35e23667c1bf65845182430d0cf0860/shared/java/impl/Platform.java].
 */
val currentPlatform: Platform
    get() {
        val os = System.getProperty("os.name").toLowerCase()

        return if (os.contains("mac") || os.contains("darwin")) {
            if ("aarch64" == System.getProperty("os.arch"))
                Platform.MacOS
            else Platform.MacOS
        } else if (os.contains("windows"))
            Platform.Windows
        else if (os.contains("nux") || os.contains("nix"))
            Platform.Linux
        else throw RuntimeException(
            "Unsupported platform: $os"
        )
    }

/**
 * Synchronously load an image file stored in resources for the application.
 * Deprecated by Compose, but the replacement doesn't give an [ImageBitmap] so it's useless.
 *
 * @param resourcePath path to the image file
 * @return the decoded image data associated with the resource
 */
@Composable
fun imageResource(resourcePath: String): ImageBitmap {
    return remember(resourcePath) {
        useResource(resourcePath, ::loadImageBitmap)
    }
}

val ServiceLocator.vmParamsManager: VmParamsManager
    get() = VmParamsManager(gamePath, currentPlatform)