package `fun`.hydd.cdda_browser.model.jsonParser

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.model.base.CddaItemRef

class CddaItemRefJsonParser() : JsonParser<CddaItemRef>() {
  override fun parse(jsonValue: Any, para: String): CddaItemRef {
    if (para.isBlank()) throw Exception("CddaItemRef miss para")
    return when (jsonValue) {
      is String -> CddaItemRef(CddaType.valueOf(para), jsonValue)
      else -> throw IllegalArgumentException("value is $jsonValue, type is ${jsonValue::class}")
    }
  }
}
