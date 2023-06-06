package `fun`.hydd.cdda_browser.model

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import io.vertx.core.json.JsonObject
import java.nio.file.Path

data class CddaCommonItem(
    val jsonType: JsonType,
    val cddaType: CddaType,
    val mod: CddaModDto,
    val path: Path,
    val json: JsonObject,
    val copyFrom: String?,
    val abstract: Boolean,
    val relative: JsonObject?,
    val proportional: JsonObject?,
    val extend: JsonObject?,
    val delete: JsonObject?,
)
