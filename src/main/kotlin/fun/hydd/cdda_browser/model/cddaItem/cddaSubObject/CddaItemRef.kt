package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject

data class CddaItemRef(
  @MapInfo(ignore = true) var type: CddaType = CddaType.NULL,
  @MapInfo(ignore = true) var id: String = ""
) : CddaSubObject() {

  override fun finalize(jsonValue: Any, param: String) {
    if (param.isBlank()) throw Exception("CddaItemRef miss param")
    when (jsonValue) {
      is String -> {
        this.type = CddaType.valueOf(param.uppercase())
        this.id = jsonValue
      }

      else -> throw IllegalArgumentException("value is $jsonValue, type is ${jsonValue::class}")
    }
  }
}
