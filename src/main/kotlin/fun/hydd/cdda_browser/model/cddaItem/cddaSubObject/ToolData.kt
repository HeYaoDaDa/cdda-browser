package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Power

data class ToolData(
  @MapInfo(param = "AMMUNITION_TYPE")
  var armor: MutableSet<CddaItemRef> = mutableSetOf(),
  var maxCharges: Int = 0,
  var initialCharges: Int = 0,
  var chargesPerUse: Int = 0,
  var chargeFactor: Int = 1,
  var turnsPerCharge: Int = 0,
  var powerDraw: Power = Power(),
  @MapInfo(param = "ITEM")
  var revertTo: CddaItemRef? = null,
  @MapInfo(key = "sub", param = "ITEM")
  var subtype: CddaItemRef? = null,
  var randCharges: MutableList<Int>? = null,
) : CddaSubObject()
