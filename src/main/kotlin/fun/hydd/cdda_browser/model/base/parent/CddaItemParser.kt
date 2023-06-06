package `fun`.hydd.cdda_browser.model.base.parent

import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.FinalResult
import `fun`.hydd.cdda_browser.model.ModOrder
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import io.vertx.core.json.JsonArray

abstract class CddaItemParser {

  //todo add finalMap
  abstract fun parse(jsonEntity: Any, dependencies: MutableMap<CddaItemRef, ModOrder>): FinalResult

  open fun parseIds(item: CddaCommonItem): Set<String> {
    return when (val idValue = item.json.getValue("id")) {
      is String -> setOf(idValue)
      is JsonArray -> idValue.mapNotNull { if (it is String) it else throw Exception("Id field is not String") }
        .toSet()

      else -> throw Exception("Id field is not String")
    }
  }
}
