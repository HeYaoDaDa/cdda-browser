package `fun`.hydd.cdda_browser.model.jsonParser

class BooleanJsonParser() : JsonParser<Boolean>() {
  override fun parse(jsonValue: Any, para: String): Boolean {
    if (jsonValue is Boolean) return jsonValue else throw IllegalArgumentException()
  }
}
