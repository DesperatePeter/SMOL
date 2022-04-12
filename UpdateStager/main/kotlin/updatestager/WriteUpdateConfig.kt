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

import org.update4j.Configuration
import update_installer.BaseAppUpdater
import update_installer.UpdateChannel
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.writer


object WriteLocalUpdateConfig {
    fun run(
        onlineUrl: String,
        directoryOfFilesToAddToManifest: Path,
        updater: BaseAppUpdater,
        channel: UpdateChannel
    ): Configuration {
        val dir = directoryOfFilesToAddToManifest
        val config = updater.createConfiguration(dir, onlineUrl)

        println("Creating config based on files in ${dir.absolutePathString()} with base url '$onlineUrl'.")
        dir.resolve(updater.getConfigXmlFileName(channel)).run {
            this.writer().use {
                config.write(it)
                println("Wrote config to ${this.absolutePathString()}")
            }
        }
        return config
    }
}