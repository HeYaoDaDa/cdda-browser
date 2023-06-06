package `fun`.hydd.cdda_browser.model

import `fun`.hydd.cdda_browser.model.base.CddaItemRef

data class ParseResult(
  val jsonEntity: Any?,
  val dependencies: MutableMap<CddaItemRef, ModOrder>?,
  val deferRef: CddaItemRef?,
) {
  fun isPass(): Boolean {
    return jsonEntity != null
  }
}
