package `fun`.hydd.cdda_browser.model

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemData
import io.vertx.core.json.JsonObject
import java.nio.file.Path

data class FinalCddaItem(
  val cddaCommonItem: CddaCommonItem,
  var jsonEntity: Any,
  var cddaItemData: CddaItemData,
  var dependencies: MutableMap<CddaItemRef, ModOrder>?,
  val id: String,
  val modOrder: ModOrder,
  val cddaType: CddaType,
  val jsonType: JsonType,
  val path: Path,
  val abstract: Boolean,
  val name: Translation?,
  val description: Translation?,
  val originalJson: JsonObject,
)
