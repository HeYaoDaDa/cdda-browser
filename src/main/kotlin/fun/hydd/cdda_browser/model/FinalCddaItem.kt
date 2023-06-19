package `fun`.hydd.cdda_browser.model

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation
import io.vertx.core.json.JsonObject
import java.nio.file.Path

data class FinalCddaItem(
  val cddaCommonItem: CddaCommonItem,
  var cddaObject: CddaObject,
  val id: String,
  val cddaType: CddaType,
  val jsonType: JsonType,
  val path: Path,
  val abstract: Boolean,
  val name: Translation?,
  val description: Translation?,
  val originalJson: JsonObject,
) {
  fun getMod(): CddaModDto {
    return cddaCommonItem.mod
  }
}
