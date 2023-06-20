package `fun`.hydd.cdda_browser.model.cddaItem

import com.fasterxml.jackson.annotation.JsonIgnore
import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation

class SubBodyPart : CddaObject() {
  lateinit var id: String
  lateinit var name: Translation

  @MapInfo(param = "BODY_PART")
  lateinit var parent: CddaItemRef
  var secondary: Boolean = false
  var maxCoverage: Int = 0

  @IgnoreMap
  var side: Side = Side.BOTH

  var nameMultiple: Translation? = null

  @MapInfo(param = "SUB_BODY_PART")
  var opposite: CddaItemRef? = null

  @MapInfo(param = "SUB_BODY_PART", spFun = "locationsUnderFun")
  var locationsUnder: MutableList<CddaItemRef> = mutableListOf()

  @JsonIgnore
  @MapInfo(key = "side", spFun = "sideJsonFun")
  var sideJson: Int = 0

  fun locationsUnderFun() {
    if (this.locationsUnder.isEmpty())
      this.locationsUnder.add(CddaItemRef(CddaType.SUB_BODY_PART, this.id))
  }

  fun sideJsonFun() {
    this.side = Side.values()[this.sideJson]
  }

  enum class Side {
    BOTH, LEFT, RIGHT
  }
}
