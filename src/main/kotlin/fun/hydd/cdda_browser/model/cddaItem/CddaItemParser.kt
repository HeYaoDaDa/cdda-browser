package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.dto.CddaItem
import `fun`.hydd.cdda_browser.dto.CddaItemData
import `fun`.hydd.cdda_browser.dto.CddaItemRef
import `fun`.hydd.cdda_browser.dto.CddaJson

abstract class CddaItemParser() {
  abstract fun parseIds(item: CddaJson): Set<String>
  fun parse(item: CddaItem, data: CddaItemData?): CddaItemRef? {
    val realData = data ?: newData()
    item.data = realData
    return doParse(item, realData)
  }

  protected abstract fun doParse(item: CddaItem, data: CddaItemData): CddaItemRef?
  protected abstract fun newData(): CddaItemData
}
