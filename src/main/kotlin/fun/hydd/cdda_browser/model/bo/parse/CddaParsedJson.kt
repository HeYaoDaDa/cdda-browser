package `fun`.hydd.cdda_browser.model.bo.parse

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import io.vertx.core.json.JsonObject
import java.io.File

class CddaParsedJson {
  lateinit var jsonType: JsonType
  lateinit var cddaType: CddaType
  lateinit var mod: CddaParseMod
  lateinit var path: File
  lateinit var json: JsonObject

  var copyFrom: String? = null
  var abstract: Boolean = false
  var relative: JsonObject? = null
  var proportional: JsonObject? = null
  var extend: JsonObject? = null
  var delete: JsonObject? = null
}
