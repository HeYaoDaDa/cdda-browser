package `fun`.hydd.cdda_browser.model.base

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory
import java.io.File

class CddaJson(
  val jsonType: JsonType,
  val cddaType: CddaType,
  val mod: CddaModDto,
  val path: File,
  val json: JsonObject,

  val copyFrom: String?,
  val abstract: Boolean,
  val relative: JsonObject?,
  val proportional: JsonObject?,
  val extend: JsonObject?,
  val delete: JsonObject?,
) {
  var id: String? = null
  var data: Any? = null

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val missJsonTypes = mutableSetOf<String>()

    fun of(jsonObject: JsonObject, mod: CddaModDto, path: File): CddaJson? {
      return if (jsonObject.containsKey("type")) {
        val type = jsonObject.getString("type")
        if (missJsonTypes.contains(type)) return null
        try {
          val jsonType = JsonType.valueOf(type)
          val cddaType = CddaType.values().first { it.jsonType.contains(jsonType) }
          CddaJson(
            jsonType,
            cddaType,
            mod,
            path,
            jsonObject,
            jsonObject.getString("copy-from"),
            jsonObject.getBoolean("abstract", false),
            jsonObject.getJsonObject("relative"),
            jsonObject.getJsonObject("proportional"),
            jsonObject.getJsonObject("extend"),
            jsonObject.getJsonObject("delete")
          )
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
