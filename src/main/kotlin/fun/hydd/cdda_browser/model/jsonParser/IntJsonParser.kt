package `fun`.hydd.cdda_browser.model.jsonParser

class IntJsonParser() : JsonParser<Int>() {
  override fun parse(jsonValue: Any, para: String): Int {
    return when (jsonValue) {
      is Int -> jsonValue
      else -> throw IllegalArgumentException("value is $jsonValue, type is ${jsonValue::class}")
    }
  }
}
