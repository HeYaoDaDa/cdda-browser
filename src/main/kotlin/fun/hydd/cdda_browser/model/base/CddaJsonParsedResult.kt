package `fun`.hydd.cdda_browser.model.base

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.io.File

class CddaJsonParsedResult() {
  lateinit var jsonType: JsonType
  lateinit var cddaType: CddaType
  lateinit var mod: CddaModParseDto
  lateinit var path: File
  lateinit var json: JsonObject

  var copyFrom: String? = null
  var abstract: Boolean = false
  var relative: JsonObject? = null
  var proportional: JsonObject? = null
  var extend: JsonObject? = null
  var delete: JsonObject? = null

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val missJsonTypes = mutableSetOf<String>()

    fun of(jsonObject: JsonObject, mod: CddaModParseDto, path: File): CddaJsonParsedResult? {
      return if (jsonObject.containsKey("type")) {
        val type = jsonObject.getString("type")
        if (missJsonTypes.contains(type)) return null
        try {
          val jsonType = JsonType.valueOf(type)
          val cddaType = CddaType.values().first { it.jsonType.contains(jsonType) }
          val cddaJsonParsedResult = CddaJsonParsedResult()
          cddaJsonParsedResult.jsonType = jsonType
          cddaJsonParsedResult.cddaType = cddaType
          cddaJsonParsedResult.mod = mod
          cddaJsonParsedResult.path = path
          cddaJsonParsedResult.json = jsonObject
          cddaJsonParsedResult.copyFrom = jsonObject.getString("copy-from")
          cddaJsonParsedResult.abstract = jsonObject.getBoolean("abstract", false)
          cddaJsonParsedResult.relative = jsonObject.getJsonObject("relative")
          cddaJsonParsedResult.proportional = jsonObject.getJsonObject("proportional")
          cddaJsonParsedResult.extend = jsonObject.getJsonObject("extend")
          cddaJsonParsedResult.delete = jsonObject.getJsonObject("delete")
          cddaJsonParsedResult
        } catch (e: IllegalArgumentException) {
          missJsonTypes.add(type)
          log.warn("$type not exits in JsonType")
          null
        }
      } else {
        null
      }
    }
  }
}
