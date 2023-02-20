package `fun`.hydd.cdda_browser.model.bo.restful.data

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.model.base.Translation
import io.vertx.core.json.JsonObject

data class CddaItemData(
  val jsonType: JsonType,
  val cddaType: CddaType,
  val modId: String,
  val id: String,
  val path: String,
  val json: JsonObject,
  val abstract: Boolean,
  val data: JsonObject,
  var name: Translation?,
  var description: Translation?
)
