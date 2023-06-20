package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import io.vertx.core.json.JsonArray

data class StrStrPair(
  @IgnoreMap var name: String = "",
  @IgnoreMap var value: String = ""
) : CddaSubObject() {
  override fun finalize(jsonValue: Any, param: String) {
    if (jsonValue is JsonArray) {
      this.name = jsonValue.list[0] as String
      this.value = jsonValue.list[1] as String
    } else {
      throw IllegalArgumentException("value is $jsonValue, type is ${jsonValue::class}")
    }
  }
}
