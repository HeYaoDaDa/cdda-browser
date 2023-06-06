package `fun`.hydd.cdda_browser.model.jsonParser

import io.vertx.core.json.JsonArray

data class StrNumPair(
  val name: String,
  val value: Double,
) {
  class JsonParser() : `fun`.hydd.cdda_browser.model.jsonParser.JsonParser<StrNumPair>() {
    override fun parse(jsonValue: Any, para: String): StrNumPair {
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
        if (name != null && value != null) return StrNumPair(name!!, value!!) else throw Exception()
      } else {
        throw IllegalArgumentException("value is $jsonValue, type is ${jsonValue::class}")
      }
    }
  }
}
