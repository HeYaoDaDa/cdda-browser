package `fun`.hydd.cdda_browser.model.base.parent

import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseItem
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParsedJson

abstract class CddaItemParser {
  fun parse(item: CddaParseItem, data: CddaItemData?): CddaItemRef? {
    val realData = data ?: newData()
    item.data = realData
    return doParse(item, realData)
  }

  abstract fun parseIds(item: CddaParsedJson): Set<String>
  protected abstract fun doParse(item: CddaParseItem, data: CddaItemData): CddaItemRef?
  protected abstract fun newData(): CddaItemData
}
