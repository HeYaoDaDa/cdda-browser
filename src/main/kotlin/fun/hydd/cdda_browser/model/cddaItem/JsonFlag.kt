package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation

@Suppress("unused")
class JsonFlag : CddaObject() {
  var info: Translation? = null
  var inherit: Boolean = true
  var craftInherit: Boolean = false
  var tasteMod: Int = 0
  var restriction: Translation? = null
  var name: Translation? = null

  @MapInfo(param = "JSON_FLAG")
  var conflicts: MutableList<CddaItemRef> = mutableListOf()

  @MapInfo(param = "JSON_FLAG")
  var requiresFlag: CddaItemRef? = null
}
