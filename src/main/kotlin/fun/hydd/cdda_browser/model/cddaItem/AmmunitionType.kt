package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation

class AmmunitionType : CddaObject() {
  lateinit var id: String

  lateinit var name: Translation

  @MapInfo(param = "ITEM")
  lateinit var default: CddaItemRef
}
