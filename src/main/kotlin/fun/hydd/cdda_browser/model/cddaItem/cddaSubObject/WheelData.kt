package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject

data class WheelData(
  var diameter: Int = 0,
  var width: Int = 0
) : CddaSubObject()
