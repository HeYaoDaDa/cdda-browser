package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject

data class MagazineData(
  @MapInfo(param = "AMMUNITION_TYPE")
  var ammoType: MutableSet<CddaItemRef> = mutableSetOf(),
  var capacity: Int = 0,
  var count: Int = 0,
  @MapInfo(param = "ITEM")
  var defaultAmmo: CddaItemRef = CddaItemRef(),
  var reloadTime: Int = 0,
  @MapInfo(param = "ITEM")
  var linkage: CddaItemRef? = null
) : CddaSubObject()
