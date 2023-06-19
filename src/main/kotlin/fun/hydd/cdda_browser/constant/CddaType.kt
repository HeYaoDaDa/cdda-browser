package `fun`.hydd.cdda_browser.constant

import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.*
import io.vertx.core.json.JsonArray
import kotlin.reflect.KClass
import kotlin.reflect.KFunction1

enum class CddaType(
  val jsonType: Set<JsonType>,
  val objectClass: KClass<out CddaObject>,
  val getIdsFun: KFunction1<CddaCommonItem, Set<String>> = ::parseCommonIds,
) {
  NULL(setOf(), ModInfo::class),
  MOD_INFO(setOf(JsonType.MOD_INFO), ModInfo::class),
  JSON_FLAG(setOf(JsonType.JSON_FLAG), JsonFlag::class),
  WEAPON_CATEGORY(setOf(JsonType.WEAPON_CATEGORY), WeaponCategory::class),
  ASCII_ART(setOf(JsonType.ASCII_ART), AsciiArt::class),
  BODY_PART(setOf(JsonType.BODY_PART), BodyPart::class),
  SUB_BODY_PART(setOf(JsonType.SUB_BODY_PART), SubBodyPart::class),
  MATERIAL(setOf(JsonType.MATERIAL), Material::class),
  ITEM_CATEGORY(setOf(JsonType.ITEM_CATEGORY), ItemCategory::class),
  AMMUNITION_TYPE(setOf(JsonType.AMMUNITION_TYPE), AmmunitionType::class),
  ITEM(
    setOf(
      JsonType.GENERIC,
      JsonType.BIONIC_ITEM,
      JsonType.BATTERY,
      JsonType.MAGAZINE,
      JsonType.GUNMOD,
      JsonType.WHEEL,
      JsonType.ENGINE,
      JsonType.COMESTIBLE,
      JsonType.BOOK,
      JsonType.TOOL_ARMOR,
      JsonType.TOOLMOD,
      JsonType.TOOL,
      JsonType.PET_ARMOR,
      JsonType.ARMOR,
      JsonType.GUN,
      JsonType.AMMO,
    ), Item::class
  );
}

fun parseCommonIds(item: CddaCommonItem): Set<String> {
  return if (item.json.containsKey("id")) {
    when (val idValue = item.json.getValue("id")) {
      is String -> setOf(idValue)
      is JsonArray -> idValue.map {
        if (it is String)
          it
        else
          throw Exception("id field is not String[], element value is $it")
      }.toSet()

      else -> throw Exception("Id field is not String or String[], value is $idValue")
    }
  } else if (item.json.containsKey("copy-from")) {
    setOf(item.json.getString("copy-from"))
  } else throw Exception("not contain key id or copy-from")
}

