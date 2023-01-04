package `fun`.hydd.cdda_browser.model.base.parent

import `fun`.hydd.cdda_browser.model.base.CddaItem
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.CddaJson

abstract class CddaItemParser {
  abstract fun parseIds(item: CddaJson): Set<String>
  fun parse(item: CddaItem, data: CddaItemData?): CddaItemRef? {
    val realData = data ?: newData()
    item.data = realData
    return doParse(item, realData)
  }

  protected abstract fun doParse(item: CddaItem, data: CddaItemData): CddaItemRef?
  protected abstract fun newData(): CddaItemData
}
