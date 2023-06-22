package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Energy

data class BatteryData(
  var maxCapacity: Energy = Energy()
) : CddaSubObject()
