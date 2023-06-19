package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation

class BodyPart : CddaObject() {
  lateinit var id: String
  lateinit var name: Translation
  var nameMultiple: Translation? = null
  var accusative: Translation? = null
  var accusativeMultiple: Translation? = null
  lateinit var heading: Translation
  lateinit var headingMultiple: Translation
  var hpBarUiText: Translation? = null
  lateinit var encumbranceText: Translation
  var hitSize: Double = 0.0
  var hitDifficulty: Double = 0.0
  var baseHp: Double = 60.0
  // todo more field finalize()

  @MapInfo(param = "JSON_FLAG")
  var flags: MutableList<CddaItemRef> = mutableListOf()

  override fun finalize(commonItem: CddaCommonItem, itemRef: CddaItemRef) {
    this.itemName = this.name
  }
}
