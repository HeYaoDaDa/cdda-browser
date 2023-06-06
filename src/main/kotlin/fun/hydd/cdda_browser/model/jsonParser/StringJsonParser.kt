package `fun`.hydd.cdda_browser.model.jsonParser

class StringJsonParser() : JsonParser<String>() {
  override fun parse(jsonValue: Any, para: String): String {
    if (jsonValue is String) return jsonValue else throw IllegalArgumentException("value is $jsonValue, type is ${jsonValue::class}")
  }
}
