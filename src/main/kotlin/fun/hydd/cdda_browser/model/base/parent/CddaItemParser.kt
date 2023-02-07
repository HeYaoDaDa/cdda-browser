package `fun`.hydd.cdda_browser.model.base.parent

import `fun`.hydd.cdda_browser.model.base.CddaItemParseDto
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.CddaJsonParsedResult

abstract class CddaItemParser {
  fun parse(item: CddaItemParseDto, data: CddaItemData?): CddaItemRef? {
    val realData = data ?: newData()
    item.data = realData
    return doParse(item, realData)
  }

  abstract fun parseIds(item: CddaJsonParsedResult): Set<String>
  protected abstract fun doParse(item: CddaItemParseDto, data: CddaItemData): CddaItemRef?
  protected abstract fun newData(): CddaItemData
}
