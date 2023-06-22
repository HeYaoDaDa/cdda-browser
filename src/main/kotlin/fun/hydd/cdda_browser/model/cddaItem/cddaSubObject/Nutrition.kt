package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject

data class Nutrition(
  var calories: Int = 0,
  var vitamins: MutableMap<CddaItemRef, Int> = mutableMapOf()
) : CddaSubObject()
