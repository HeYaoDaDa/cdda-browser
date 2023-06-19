package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import io.vertx.core.json.JsonArray

data class StrNumPair(
  @MapInfo(ignore = true) var name: String = "",
  @MapInfo(ignore = true) var value: Double = 0.0
) : CddaSubObject() {
  override fun finalize(jsonValue: Any, param: String) {
    if (jsonValue is JsonArray) {
      var name: String? = null
      var value: Double? = null
      jsonValue.forEachIndexed { index, it ->
        when (index) {
          0 -> name = it as String
          1 -> value = when (it) {
            is Double -> it
            is Float -> it.toDouble()
            is Int -> it.toDouble()
            else -> throw Exception()
          }

          else -> throw Exception()
        }
      }
      if (name != null && value != null) {
        this.name = name as String
        this.value = value as Double
      } else throw Exception("value is $jsonValue, miss name or value")
    } else {
      throw IllegalArgumentException("value is $jsonValue, type is ${jsonValue::class}")
    }
  }
}
