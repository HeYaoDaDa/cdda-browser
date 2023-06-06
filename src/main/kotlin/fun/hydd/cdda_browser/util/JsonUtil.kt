package `fun`.hydd.cdda_browser.util

import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import java.io.File
import kotlin.collections.set

object JsonUtil {
  /**
   * Get json file all JsonObject
   *
   * @param fileSystem vertx fileSystem
   * @param file json file
   * @return JsonObjects
   */
  suspend fun getJsonObjectsByFile(fileSystem: FileSystem, file: File): List<JsonObject> {
    return when (val buffer = fileSystem.readFile(file.absolutePath).await().toJson()) {
      is JsonArray -> buffer.mapNotNull { if (it is JsonObject) it else null }
      is JsonObject -> listOf(buffer)
      else -> throw Exception("Result class isn't JsonObject or JsonArray")
    }
  }

  /**
   * recursive sort jsonObject key-value by key, no parameter modification
   * @param jsonObject pending sort JsonObject
   * @return sored JsonObject
   */
  fun sortJsonObject(jsonObject: JsonObject): JsonObject {
    val sortedMap = LinkedHashMap<String, Any?>(jsonObject.size())
    for (entity in jsonObject.map.entries.sortedBy { it.key }) {
      val value = entity.value
      if (value != null) {
        sortedMap[entity.key] = when (value) {
          is JsonObject -> sortJsonObject(value)
          is JsonArray -> sortJsonArray(value)
          else -> value
        }
      } else {
        sortedMap[entity.key] = null
      }
    }
    return JsonObject(sortedMap)
  }

  /**
   * recursive sort jsonArray's JsonObject key-value, detail: [sortJsonObject]
   * @param jsonArray pending sort JsonArray
   * @return sored JsonArray
   */
  fun sortJsonArray(jsonArray: JsonArray): JsonArray {
    return JsonArray(
      jsonArray.toList().stream().map {
        when (it) {
          is JsonObject -> sortJsonObject(it)
          is JsonArray -> sortJsonArray(it)
          else -> it
        }
      }.toList()
    )
  }
}


