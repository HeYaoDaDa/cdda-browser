package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation

class ItemCategory : CddaObject() {
  lateinit var id: String

  lateinit var name: Translation

  var sortRank: Int = 0
}
