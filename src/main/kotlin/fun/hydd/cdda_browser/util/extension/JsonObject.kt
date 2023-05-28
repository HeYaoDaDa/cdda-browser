package `fun`.hydd.cdda_browser.util.extension

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.util.JsonUtil
import `fun`.hydd.cdda_browser.util.StringUtil
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import java.lang.instrument.IllegalClassFormatException

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

fun JsonObject.getCddaItemRef(key: String, cddaType: CddaType): CddaItemRef? {
  val id = this.getString(key)
  return if (id != null) CddaItemRef(cddaType, id) else null
}

fun JsonObject.getCddaItemRefs(key: String, cddaType: CddaType): Collection<CddaItemRef>? {
  return this.getOrCreateJsonArray(key)?.mapNotNull {
    if (it is String) CddaItemRef(cddaType, it)
    else throw Exception("$it class is ${it::class} not is CddaItemRef")
  }
}

/**
 * get length 64 sha256 hash code
 * @receiver JsonObject
 * @return String hash code, length 64
 */
fun JsonObject.getHashString(): String {
  return StringUtil.getStringHash(JsonUtil.sortJsonObject(this).toString())
}
