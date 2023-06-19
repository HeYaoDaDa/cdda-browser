package `fun`.hydd.cdda_browser.model

import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef

data class FinalResult(
  val cddaObject: CddaObject?,
  val deferRef: CddaItemRef?,
) {
  fun isPass(): Boolean {
    return cddaObject != null
  }
}
