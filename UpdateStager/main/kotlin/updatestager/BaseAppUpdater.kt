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

package updatestager

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.update4j.Configuration
import org.update4j.FileMetadata
import org.update4j.UpdateOptions
import smol_access.Constants
import smol_access.config.AppConfig
import timber.ktx.Timber
import java.net.URI
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name

/**
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
abstract class BaseAppUpdater() {
    /**
     * Percent of download done between 0 and 1.
     */
    val totalDownloadFraction = MutableStateFlow<Float?>(null)
    val totalDownloadBytes = MutableStateFlow<Long?>(null)
    val totalDownloadedBytes = MutableStateFlow<Long>(0)
    val currentFileDownload = MutableStateFlow<FileDownload?>(null)
    protected abstract val configXmlBaseFileNameWithoutExtension: String
    abstract val versionPropertyKey: String
    abstract val updateZipFile: Path

    data class FileDownload(val name: String, val progress: Float)
    enum class UpdateChannel {
        Stable,
        Unstable,
        Test,
    }

    open fun getConfigXmlFileName(channel: UpdateChannel) =
        when (channel) {
            UpdateChannel.Stable -> "$configXmlBaseFileNameWithoutExtension-main.xml"
            UpdateChannel.Unstable -> "$configXmlBaseFileNameWithoutExtension-unstable.xml"
            UpdateChannel.Test -> "$configXmlBaseFileNameWithoutExtension-test.xml"
        }

    /**
     * Sets the release channel for the user.
     */
    open fun setUpdateChannel(updateChannel: UpdateChannel, appConfig: AppConfig) {
        appConfig.updateChannel = when (updateChannel) {
            UpdateChannel.Stable -> AppConfig.UpdateChannel.Stable
            UpdateChannel.Unstable -> AppConfig.UpdateChannel.Unstable
            UpdateChannel.Test -> AppConfig.UpdateChannel.Test
        }
    }

    open fun getUpdateChannelSetting(appConfig: AppConfig): UpdateChannel = Companion.getUpdateChannelSetting(appConfig)

    /**
     * Gets the url of the files for the specified channel (ie the branch on GitHub).
     */
    fun getUpdateConfigBaseUrl(channel: UpdateChannel): String =
        when (channel) {
            UpdateChannel.Stable -> Constants.UPDATE_URL_STABLE
            UpdateChannel.Unstable -> Constants.UPDATE_URL_UNSTABLE
            UpdateChannel.Test -> Constants.UPDATE_URL_TEST
        }

    /**
     * Gets the url of the remote [Configuration].
     */
    open fun getRemoteConfigUrl(channel: UpdateChannel): URL =
        URI.create("${getUpdateConfigBaseUrl(channel)}/${getConfigXmlFileName(channel)}").toURL()

    /**
     * Creates a [Configuration] object based on local files. Essentially, this is a file manifest.
     * @param directoryOfFilesToAddToManifest Files added to the [Configuration] will have paths relative to this folder.
     */
    abstract fun createConfiguration(directoryOfFilesToAddToManifest: Path, remoteConfigUrl: String): Configuration

    /**
     * Downloads the [Configuration] file from GitHub for the specified release channel.
     */
    suspend fun fetchRemoteConfig(appConfig: AppConfig): Configuration =
        fetchRemoteConfig(getUpdateChannelSetting(appConfig))

    /**
     * Downloads the [Configuration] file from GitHub for the specified release channel.
     */
    open suspend fun fetchRemoteConfig(channel: UpdateChannel): Configuration {
        val remoteConfigUrl = getRemoteConfigUrl(channel)

        Timber.i { "Fetching ${getConfigXmlFileName(channel)} from ${remoteConfigUrl}." }
        val remoteConfig = withContext(Dispatchers.IO) {
            runCatching {
                remoteConfigUrl.openStream().use { stream ->
                    Configuration.read(stream.bufferedReader())
                }
            }
                .onFailure { Timber.w(it) }
                .onSuccess {
                    Timber.i {
                        "Fetched ${getConfigXmlFileName(channel)} from ${remoteConfigUrl}. Update needed? ${it.requiresUpdate()}, Total size: ${
                            it.files.filter { it.requiresUpdate() }.sumOf { it.size }
                        }b."
                    }
                }
                .getOrThrow()
        }

        totalDownloadBytes.value = remoteConfig?.files
            ?.filter { it?.requiresUpdate() ?: false }
            ?.filterNotNull()
            ?.sumOf { it.size }

        return remoteConfig
    }

    /**
     * Download a zip with the files that need to be updated.
     */
    open suspend fun downloadUpdateZip(remoteConfig: Configuration): Path? {
        if (isUpdatedDownloaded()) {
            Timber.i { "Update already exists." }
            return updateZipFile
        } else {
            Timber.i { "Fetching update from ${remoteConfig.baseUri}." }
            withContext(Dispatchers.IO) {
                remoteConfig.update(
                    UpdateOptions
                        .archive(updateZipFile)
                        .updateHandler(object : SmolUpdateHandler() {
                            override fun updateDownloadFileProgress(file: FileMetadata?, frac: Float) {
                                if (!isActive) {
                                    currentFileDownload.value = null
                                    throw CancellationException("Update coroutine was canceled.")
                                }

                                super.updateDownloadFileProgress(file, frac)
                                currentFileDownload.value =
                                    FileDownload(name = file?.path?.name ?: "(unknown)", progress = frac)
                            }

                            override fun updateDownloadProgress(frac: Float) {
                                if (!isActive) {
                                    totalDownloadFraction.value = null
                                    throw CancellationException("Update coroutine was canceled.")
                                }

                                super.updateDownloadProgress(frac)
                                totalDownloadFraction.value = frac
                            }

                            override fun doneDownloadFile(file: FileMetadata, tempFile: Path) {
                                super.doneDownloadFile(file, tempFile)
                                totalDownloadedBytes.value += file.size
                            }

                            override fun stop() {
                                super.stop()
                                totalDownloadFraction.value = null
                            }

                            override fun failed(t: Throwable) {
                                super.failed(t)
                                totalDownloadFraction.value = null
                                currentFileDownload.value = null
                                throw t
                            }
                        })
                )
            }
        }

        return if (isUpdatedDownloaded()) updateZipFile else null
    }

    /**
     * Whether an update zip has been downloaded.
     */
    open fun isUpdatedDownloaded(): Boolean = updateZipFile.exists()

    /**
     * Install the downloaded update zip (on a separate thread).
     *
     * This will FAIL if the files being updated are in use.
     * Call this, then immediately close SMOL if this is called from SMOL.
     *
     * Throws any exceptions.
     */
    protected abstract fun installUpdateInternal()

    fun installUpdate() {
        installUpdateInternal()
        Timber.i { "Update complete for ${this::class.simpleName}." }
    }

    companion object {
        /**
         * Gets the current channel (eg stable/unstable).
         */
        fun getUpdateChannelSetting(appConfig: AppConfig): UpdateChannel {
            return when (appConfig.updateChannel) {
                AppConfig.UpdateChannel.Stable -> UpdateChannel.Stable
                AppConfig.UpdateChannel.Unstable -> UpdateChannel.Unstable
                AppConfig.UpdateChannel.Test -> UpdateChannel.Test
            }
        }
    }
}