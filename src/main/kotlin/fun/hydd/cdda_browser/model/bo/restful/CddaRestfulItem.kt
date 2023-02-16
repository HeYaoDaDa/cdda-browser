package `fun`.hydd.cdda_browser.model.bo.restful

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import io.vertx.core.json.JsonObject

data class CddaRestfulItem(
  val jsonType: JsonType,
  val cddaType: CddaType,
  val modId: String,
  val id: String,
  val path: String,
  val json: JsonObject,
  val abstract: Boolean,
  val data: JsonObject
)
