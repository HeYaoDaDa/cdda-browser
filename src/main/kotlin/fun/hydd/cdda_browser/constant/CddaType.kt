package `fun`.hydd.cdda_browser.constant

import `fun`.hydd.cdda_browser.model.base.parent.CddaItemParser
import `fun`.hydd.cdda_browser.model.cddaItem.BodyPart
import `fun`.hydd.cdda_browser.model.cddaItem.JsonFlag
import `fun`.hydd.cdda_browser.model.cddaItem.Material
import `fun`.hydd.cdda_browser.model.cddaItem.ModInfo
import kotlin.reflect.KClass

enum class CddaType(val jsonType: Set<JsonType>, val jsonEntity: KClass<out Any>, val parser: CddaItemParser) {
  MOD_INFO(setOf(JsonType.MOD_INFO), ModInfo.JsonEntity::class, ModInfo.Parser()),
  JSON_FLAG(setOf(JsonType.JSON_FLAG), JsonFlag.JsonEntity::class, JsonFlag.Parser()),
  BODY_PART(setOf(JsonType.BODY_PART), BodyPart.JsonEntity::class, BodyPart.Parser()),
  MATERIAL(setOf(JsonType.MATERIAL), Material.JsonEntity::class, Material.Parser()),
}
