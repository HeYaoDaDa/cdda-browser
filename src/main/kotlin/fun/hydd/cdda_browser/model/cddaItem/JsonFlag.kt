package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation

class JsonFlag : CddaObject() {
  lateinit var id: String
  var info: Translation? = null
  var inherit: Boolean = true
  var craftInherit: Boolean = false
  var tasteMod: Double? = null
  var restriction: Translation? = null
  var name: Translation? = null

  @MapInfo(param = "JSON_FLAG")
  var conflicts: MutableList<CddaItemRef> = mutableListOf()

  @MapInfo(param = "JSON_FLAG")
  var requiresFlag: CddaItemRef? = null

  override fun finalize(commonItem: CddaCommonItem, itemRef: CddaItemRef): CddaItemRef? {
    this.itemName = this.name
    return null
  }
}
