package `fun`.hydd.cdda_browser.model.jsonParser

class DoubleJsonParser() : JsonParser<Double>() {
  override fun parse(jsonValue: Any, para: String): Double {
    return when (jsonValue) {
      is Double -> jsonValue.toDouble()
      is Float -> jsonValue.toDouble()
      is Int -> jsonValue.toDouble()
      else -> throw IllegalArgumentException("value is $jsonValue, type is ${jsonValue::class}")
    }
  }
}
