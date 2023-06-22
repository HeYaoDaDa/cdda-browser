package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.DamageInstance
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Time
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Volume
import io.vertx.core.json.JsonArray

data class GunData(
  @MapInfo(param = "NULL")//todo skill
  var skill: CddaItemRef = CddaItemRef(),
  @MapInfo(param = "AMMUNITION_TYPE")
  var ammo: MutableSet<CddaItemRef> = mutableSetOf(),
  var range: Int = 0,
  var rangedDamage: DamageInstance? = null,
  var dispersion: Int = 0,
  var sightDispersion: Int = 30,
  var recoil: Int = 0,
  var handling: Int = -1,
  var durability: Int = 0,
  var loudness: Int = 0,
  var clipSize: Int = 0,
  @MapInfo(param = "MOVE")
  var reload: Time = Time(1),
  var reloadNoiseVolume: Int = 0,
  var barrelVolume: Volume = Volume(),
  @MapInfo(param = "ITEM")
  var builtInMods: MutableSet<CddaItemRef> = mutableSetOf(),
  @MapInfo(param = "ITEM")
  var defaultInMods: MutableSet<CddaItemRef> = mutableSetOf(),
  var upsCharges: Int = 0,
  var blackpowderTolerance: Int = 0,
  var minCycleRecoil: Int = 0,
  var ammoEffects: MutableSet<String> = mutableSetOf(),
  var ammoToFire: Int = 1,
  var validModLocations: MutableMap<Translation, Int> = mutableMapOf(),
  var modes: MutableList<GunModeData> = mutableListOf(),
) : CddaSubObject() {
  data class GunModeData(
    @IgnoreMap var id: String = "",
    @IgnoreMap var name: Translation = Translation(),
    @IgnoreMap var num: Int = 1,
    @IgnoreMap var flags: MutableList<String> = mutableListOf()
  ) : CddaSubObject() {
    override fun finalize(jsonValue: Any, param: String) {
      if (jsonValue is JsonArray) {
        jsonValue.forEachIndexed { index, it ->
          when (index) {
            0 -> this.id = it as String
            1 -> this.name = Translation(it as String)
            2 -> this.num = it as Int
            3 -> {
              when (it) {
                is String -> this.flags.add(it)
                is JsonArray -> this.flags = it.map { it as String }.toMutableList()
                else -> throw Exception("$it is not String or JsonArray")
              }
            }
          }
        }
      }
    }
  }
}
