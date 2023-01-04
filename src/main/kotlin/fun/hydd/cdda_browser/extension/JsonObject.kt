package `fun`.hydd.cdda_browser.extension

import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.util.JsonUtil
import `fun`.hydd.cdda_browser.util.StringUtil
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import java.lang.instrument.IllegalClassFormatException

inline fun <reified T : Any> JsonObject.getCollection(key: String): Collection<T>? {
  return this.getJsonArray(key)?.mapNotNull {
    if (it is T) it
    else if (Double::class == T::class && it is Int) {
      it.toDouble() as T
    } else throw Exception("$it class is ${it::class} not is ${T::class}")
  }
}

inline fun <reified T : Any> JsonObject.getCollection(key: String, def: Collection<T>): Collection<T> {
  return this.getCollection(key) ?: def
}

fun JsonObject.getTranslation(key: String, ctxt: String? = null): Translation? {
  val value = this.get<Any?>(key)
  return if (value != null) {
    when (value) {
      is String -> Translation(value, ctxt)
      is JsonObject -> Translation(value, ctxt)
      else -> throw IllegalClassFormatException("json $value's class not is jsonObject or String")
    }
  } else null
}

fun JsonObject.getTranslation(key: String, ctxt: String? = null, def: Translation): Translation {
  return this.getTranslation(key, ctxt) ?: def
}

/**
 * get length 64 sha256 hash code
 * @receiver JsonObject
 * @return String hash code, length 64
 */
fun JsonObject.getHashString(): String {
  return StringUtil.getStringHash(JsonUtil.sortJsonObject(this).toString())
}
