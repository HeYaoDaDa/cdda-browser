package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.ProcessContext
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject

data class BionicData(
  @MapInfo(spFun = "bionicIdFun")
  var bionicId: String = "",
  var difficulty: Int = 0,
  var isUpgrade: Boolean = false,
  @MapInfo(param = "ITEM")
  var installationData: CddaItemRef? = null
) : CddaSubObject() {
  fun bionicIdFun() {
    if (this.bionicId.isBlank()) {
      this.bionicId = ProcessContext.itemId!!
    }
  }
}
