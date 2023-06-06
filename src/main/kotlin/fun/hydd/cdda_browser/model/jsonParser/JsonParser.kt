package `fun`.hydd.cdda_browser.model.jsonParser

import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.model.cddaItem.BodyPart
import `fun`.hydd.cdda_browser.model.cddaItem.Material
import `fun`.hydd.cdda_browser.model.jsonParser.unit.Volume
import kotlin.reflect.KClass

abstract class JsonParser<T : Any>() {
  abstract fun parse(jsonValue: Any, para: String): T
  open fun relative(value: Any, relativeValue: Any): T {
    throw UnsupportedOperationException()
  }

  open fun proportional(value: Any, proportionalValue: Any): T {
    throw UnsupportedOperationException()
  }

  companion object {
    val jsonParsers = mapOf<KClass<*>, JsonParser<*>>(
      Pair(String::class, StringJsonParser()),
      Pair(Boolean::class, BooleanJsonParser()),
      Pair(Double::class, DoubleJsonParser()),
      Pair(Translation::class, TranslationJsonParser()),
      Pair(CddaItemRef::class, CddaItemRefJsonParser()),
      Pair(StrNumPair::class, StrNumPair.JsonParser()),
      Pair(Volume::class, Volume.JsonParser()),
      Pair(Material.MatBurnData::class, Material.MatBurnData.JsonParser()),
      Pair(Material.FuelData::class, Material.FuelData.JsonParser()),
      Pair(BodyPart.StatHpMod::class, BodyPart.StatHpMod.JsonParser()),
      Pair(BodyPart.LimbType::class, BodyPart.LimbType.JsonParser()),
    )
  }
}
