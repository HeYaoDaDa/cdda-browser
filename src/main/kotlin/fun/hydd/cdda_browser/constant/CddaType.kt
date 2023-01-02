package `fun`.hydd.cdda_browser.constant

import `fun`.hydd.cdda_browser.model.cddaItem.CddaItemParser
import `fun`.hydd.cdda_browser.model.cddaItem.ModInfo

enum class CddaType(val jsonType: Set<JsonType>, val parser: CddaItemParser) {
  MOD_INFO(setOf(JsonType.MOD_INFO), ModInfo.Parser())
}
