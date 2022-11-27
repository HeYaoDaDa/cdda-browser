package `fun`.hydd.cdda_browser.util

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import java.lang.instrument.IllegalClassFormatException

/**
 * recursive sort jsonObject key-value by key, no parameter modification
 * @param jsonObject pending sort JsonObject
 * @return sored JsonObject
 */
fun sortJsonObject(jsonObject: JsonObject): JsonObject {
  val sortedMap = LinkedHashMap<String, Any>(jsonObject.size())
  for (entity in jsonObject.map.entries.sortedBy { it.key }) {
    val value = entity.value
    sortedMap[entity.key] = when (value) {
      is JsonObject -> sortJsonObject(value)
      is JsonArray -> sortJsonArray(value)
      else -> value
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

private fun <T> getList(jsonObject: JsonObject, key: String, cla: Class<T>, def: List<T>? = null): List<T>? {
  return jsonObject.getJsonArray(key)?.mapNotNull {
    @Suppress("UNCHECKED_CAST") if (it::class.java == cla) it as T
    else throw IllegalArgumentException("$it class not is $cla")
  } ?: def
}

fun JsonObject.getStringList(key: String, def: List<String>? = null): List<String>? {
  return getList(this, key, String::class.java, def)
}

fun JsonObject.getIntList(key: String, def: List<Int>? = null): List<Int>? {
  val jsonArray = this.getJsonArray(key)
  return jsonArray?.map { if (it is Integer) it.toInt() else throw IllegalClassFormatException("$it not is Integer") }
    ?: def
}

fun JsonObject.getDoubleList(key: String, def: List<Double>? = null): List<Double>? {
  val jsonArray = this.getJsonArray(key)
  return jsonArray?.map {
    when (it) {
      is java.lang.Double -> it.toDouble()
      is java.lang.Float -> it.toDouble()
      is Integer -> it.toDouble()
      else -> throw IllegalClassFormatException("$it not is number")
    }
  } ?: def
}

fun JsonObject.getBooleanList(key: String, def: List<Boolean>? = null): List<Boolean>? {
  return getList(this,
    key,
    java.lang.Boolean::class.java,
    def?.map { java.lang.Boolean(it) })?.map { it.booleanValue() }
}

/**
 * get length 64 sha256 hash code
 * @receiver JsonObject
 * @return String hash code, length 64
 */
fun JsonObject.getHashString(): String {
  return getStringHash(sortJsonObject(this).toString())
}

/**
 * get length 64 sha256 hash code
 * @receiver JsonArray
 * @return String's hash code, length 64
 */
fun JsonArray.getHashString(): String {
  return getStringHash(sortJsonArray(this).toString())
}
