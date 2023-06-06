package `fun`.hydd.cdda_browser.model

import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemData

data class FinalResult(
  val cddaItemData: CddaItemData?,
  val dependencies: MutableMap<CddaItemRef, ModOrder>?,
  val deferRef: CddaItemRef?,
) {
  fun isPass(): Boolean {
    return cddaItemData != null
  }
}
