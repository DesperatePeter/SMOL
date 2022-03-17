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

package smol_access

import utilities.toPathOrNull
import java.nio.file.Path
import kotlin.io.path.Path

object Constants {
    const val APP_NAME = "SMOL"
    const val APP_NAME_LONG = "Starsector Mod Organizer and Launcher"
    var APP_VERSION = "" // ok it's not a constant, sue me

    // Only works when running from Compose app, not from a pure Java app.
    private val resourcesDir = System.getProperty("compose.application.resources.dir")?.toPathOrNull()

    const val MOD_INFO_FILE = "mod_info.json"

    // Backwards compat, first one is the one used for new disable actions.
    val MOD_INFO_FILE_DISABLED_NAMES = arrayOf("mod_info.json.disabled-by-SMOL", "mod_info.json.disabled")
    const val ENABLED_MODS_FILENAME = "enabled_mods.json"
    const val MODS_FOLDER_NAME = "mods"
    const val SAVES_FOLDER_NAME = "saves"
    fun getGameLogPath(gamePath: Path) = gamePath.resolve("starsector-core/starsector.log")
    const val VERSION_CHECKER_CSV_PATH = "data/config/version/version_files.csv"
    const val VERSION_CHECKER_FILE_ENDING = ".version"
    const val FORUM_URL = "https://fractalsoftworks.com/forum/index.php"
    const val NEXUS_MODS_PAGE_URL = "https://www.nexusmods.com/starsector/mods/"
    const val FORUM_MOD_PAGE_URL = "$FORUM_URL?topic="
    const val FORUM_MOD_INDEX_URL = FORUM_MOD_PAGE_URL + "177"
    const val FORUM_MODDING_SUBFORUM_URL = "$FORUM_URL?board=3.0"
    const val FORUM_HOSTNAME = "fractalsoftworks.com"

    const val APP_FOLDER_NAME = "SMOL-data"
    val APP_FOLDER_DEFAULT: Path = Path("", APP_FOLDER_NAME)
    val STAGING_FOLDER_DEFAULT: Path = APP_FOLDER_DEFAULT.resolve("staging")
    val UI_CONFIG_PATH: Path = APP_FOLDER_DEFAULT.resolve("SMOL_UIConfig.json")
    val APP_CONFIG_PATH: Path = APP_FOLDER_DEFAULT.resolve("SMOL_AppConfig.json")
    internal val BASE_THEME_CONFIG_PATH: Path? = resourcesDir?.resolve("SMOL_Themes.json")
    internal val USER_THEME_CONFIG_PATH: Path? = APP_FOLDER_DEFAULT.resolve("SMOL_UserThemes.json")
    val VERCHECK_CACHE_PATH: Path = APP_FOLDER_DEFAULT.resolve("SMOL_VerCheckCache.json")
    val VRAM_CHECKER_RESULTS_PATH: Path = APP_FOLDER_DEFAULT.resolve("SMOL_VRAMCheckResults.json")

    const val SMOL_RELEASES_URL = "https://github.com/davidwhitman/SMOL_Dist/releases"

    // Mod Repo
    const val modRepoUrl = "https://raw.githubusercontent.com/davidwhitman/StarsectorModRepo/main/ModRepo.json"

    // Updater
    val VERSION_PROPERTIES_FILE: Path? = resourcesDir?.resolve("version.properties")
    private const val baseUpdateUrl = "https://raw.githubusercontent.com/davidwhitman/SMOL_Dist"
    const val UPDATE_URL_STABLE = "$baseUpdateUrl/main"
    const val UPDATE_URL_UNSTABLE = "$baseUpdateUrl/unstable"
    const val UPDATE_URL_TEST = "$baseUpdateUrl/test"

    val TEMP_DIR = System.getProperty("java.io.tmpdir")?.let { Path.of(it) } ?: APP_FOLDER_DEFAULT

    const val TAG_TRACE = "trace"
}