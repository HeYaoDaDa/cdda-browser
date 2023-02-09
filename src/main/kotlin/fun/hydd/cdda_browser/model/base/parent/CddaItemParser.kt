package `fun`.hydd.cdda_browser.model.base.parent

import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseItem
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParsedJson
import io.vertx.core.json.JsonArray

abstract class CddaItemParser {
  fun parse(item: CddaParseItem, data: CddaItemData?): CddaItemRef? {
    val parent = data != null
    val realData = data ?: newData()
    item.data = realData
    return doParse(item, realData, parent)
  }

  open fun parseIds(item: CddaParsedJson): Set<String> {
    return when (val idValue = item.json.getValue("id")) {
      is String -> setOf(idValue)
      is JsonArray -> idValue.mapNotNull { if (it is String) it else throw Exception("Id field is not String") }
        .toSet()

      else -> throw Exception("Id field is not String")
    }
  }

  protected abstract fun doParse(item: CddaParseItem, data: CddaItemData, parent: Boolean): CddaItemRef?
  protected abstract fun newData(): CddaItemData
}
