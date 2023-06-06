package `fun`.hydd.cdda_browser.model.jsonParser.unit

data class Volume(val value: Int) {
  class JsonParser() : `fun`.hydd.cdda_browser.model.jsonParser.JsonParser<Volume>() {
    override fun parse(jsonValue: Any, para: String): Volume {
      return when (jsonValue) {
        is String ->
          if (jsonValue.lowercase().endsWith("ml")) Volume(jsonValue.dropLast(2).trim().toInt())
          else if (jsonValue.lowercase().endsWith("l")) Volume((jsonValue.dropLast(1).trim().toDouble() * 1000).toInt())
          else throw Exception("Volume unit not ml or L, value is $jsonValue")

        is Double -> Volume(jsonValue.toInt())
        is Float -> Volume(jsonValue.toInt())
        is Int -> Volume(jsonValue.toInt())
        else -> throw IllegalArgumentException()
      }
    }
  }
}
