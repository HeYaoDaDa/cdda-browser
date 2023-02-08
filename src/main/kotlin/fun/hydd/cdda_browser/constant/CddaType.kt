package `fun`.hydd.cdda_browser.constant

import `fun`.hydd.cdda_browser.model.base.parent.CddaItemParser
import `fun`.hydd.cdda_browser.model.cddaItem.JsonFlag
import `fun`.hydd.cdda_browser.model.cddaItem.ModInfo

enum class CddaType(val jsonType: Set<JsonType>, val parser: CddaItemParser) {
  MOD_INFO(setOf(JsonType.MOD_INFO), ModInfo.Parser()),
  JSON_FLAG(setOf(JsonType.JSON_FLAG), JsonFlag.Parser())
}
