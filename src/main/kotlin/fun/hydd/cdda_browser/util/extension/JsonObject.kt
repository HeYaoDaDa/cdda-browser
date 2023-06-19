package `fun`.hydd.cdda_browser.util.extension

import `fun`.hydd.cdda_browser.util.JsonUtil
import `fun`.hydd.cdda_browser.util.StringUtil
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

inline fun <reified T : Any> JsonObject.getCollection(key: String): Collection<T>? {
  return this.getOrCreateJsonArray(key)?.mapNotNull {
    if (it is T) it
    else if (Double::class == T::class && it is Int) {
      it.toDouble() as T
    } else throw Exception("$it class is ${it::class} not is ${T::class}")
  }
}

fun JsonObject.getOrCreateJsonArray(key: String): JsonArray? {
  return if (this.containsKey(key)) {
    val value = this.getValue(key)
    if (value is JsonArray) value
    else JsonArray.of(value)
  } else {
    null
  }
}

inline fun <reified T : Any> JsonObject.getCollection(key: String, def: Collection<T>): Collection<T> {
  return this.getCollection(key) ?: def
}

inline fun <reified T : Any> JsonObject.getSet(
  key: String
): MutableSet<T>? {
  return this.getCollection<T>(key)?.toMutableSet()
}

/**
 * get length 64 sha256 hash code
 * @receiver JsonObject
 * @return String hash code, length 64
 */
fun JsonObject.getHashString(): String {
  return StringUtil.getStringHash(JsonUtil.sortJsonObject(this).toString())
}
