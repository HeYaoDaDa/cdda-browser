package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject

data class ShrapnelData(
  var casingMass: Int = 0,
  var fragmentMass: Double = 0.08,
  var recovery: Int = 0,
  @MapInfo(param = "ITEM")
  var drop: CddaItemRef? = null
) : CddaSubObject()
