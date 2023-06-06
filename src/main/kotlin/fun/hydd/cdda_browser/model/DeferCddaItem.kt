package `fun`.hydd.cdda_browser.model

import `fun`.hydd.cdda_browser.model.base.CddaItemRef

data class DeferCddaItem(
  val commonItem: CddaCommonItem,
  val jsonEntity: Any?,
  val dependencies: MutableMap<CddaItemRef, ModOrder>?,
  val modOrder: ModOrder,
  val id: String,
)
