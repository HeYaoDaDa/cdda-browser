package `fun`.hydd.cdda_browser.model.base.parent

import `fun`.hydd.cdda_browser.model.base.CddaItemParseDto
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.CddaJsonParseDto

abstract class CddaItemParser {
  abstract fun parseIds(item: CddaJsonParseDto): Set<String>
  fun parse(item: CddaItemParseDto, data: CddaItemData?): CddaItemRef? {
    val realData = data ?: newData()
    item.data = realData
    return doParse(item, realData)
  }

  protected abstract fun doParse(item: CddaItemParseDto, data: CddaItemData): CddaItemRef?
  protected abstract fun newData(): CddaItemData
}
