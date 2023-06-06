package `fun`.hydd.cdda_browser.model.jsonParser.unit

data class Energy(val value: Int) {
  class JsonParser() : `fun`.hydd.cdda_browser.model.jsonParser.JsonParser<Energy>() {
    override fun parse(jsonValue: Any, para: String): Energy {
      return when (jsonValue) {
        is String ->
          if (jsonValue.lowercase().endsWith("kj")) Energy((jsonValue.dropLast(2).trim().toDouble() * 1000).toInt())
          else if (jsonValue.lowercase().endsWith("j")) Energy(jsonValue.dropLast(1).trim().toInt())
          else throw Exception("Volume unit not j or kj, value is $jsonValue")

        is Double -> Energy(jsonValue.toInt())
        is Float -> Energy(jsonValue.toInt())
        is Int -> Energy(jsonValue.toInt())
        else -> throw IllegalArgumentException()
      }
    }
  }
}
