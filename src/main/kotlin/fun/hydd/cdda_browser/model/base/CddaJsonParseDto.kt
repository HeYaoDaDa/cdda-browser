package `fun`.hydd.cdda_browser.model.base

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.io.File

class CddaJsonParseDto() {
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

    fun of(jsonObject: JsonObject, mod: CddaModParseDto, path: File): CddaJsonParseDto? {
      return if (jsonObject.containsKey("type")) {
        val type = jsonObject.getString("type")
        if (missJsonTypes.contains(type)) return null
        try {
          val jsonType = JsonType.valueOf(type)
          val cddaType = CddaType.values().first { it.jsonType.contains(jsonType) }
          val cddaJsonParseDto = CddaJsonParseDto()
          cddaJsonParseDto.jsonType = jsonType
          cddaJsonParseDto.cddaType = cddaType
          cddaJsonParseDto.mod = mod
          cddaJsonParseDto.path = path
          cddaJsonParseDto.json = jsonObject
          cddaJsonParseDto.copyFrom = jsonObject.getString("copy-from")
          cddaJsonParseDto.abstract = jsonObject.getBoolean("abstract", false)
          cddaJsonParseDto.relative = jsonObject.getJsonObject("relative")
          cddaJsonParseDto.proportional = jsonObject.getJsonObject("proportional")
          cddaJsonParseDto.extend = jsonObject.getJsonObject("extend")
          cddaJsonParseDto.delete = jsonObject.getJsonObject("delete")
          cddaJsonParseDto
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
