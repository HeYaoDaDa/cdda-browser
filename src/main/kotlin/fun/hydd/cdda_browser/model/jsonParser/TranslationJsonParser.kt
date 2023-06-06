package `fun`.hydd.cdda_browser.model.jsonParser

import `fun`.hydd.cdda_browser.model.base.Translation
import io.vertx.core.json.JsonObject

class TranslationJsonParser() : JsonParser<Translation>() {
  override fun parse(jsonValue: Any, para: String): Translation {
    return when (jsonValue) {
      is String -> Translation(jsonValue, para.ifBlank { null })
      is JsonObject -> Translation(jsonValue, para.ifBlank { null })
      else -> throw IllegalArgumentException("value is $jsonValue, type is ${jsonValue::class}")
    }
  }
}
