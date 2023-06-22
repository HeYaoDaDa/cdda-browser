package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.DamageInstance
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Time

data class GunModData(
  var damageModifier: DamageInstance = DamageInstance(),
  var loudnessModifier: Int = 0,
  var location: String? = null,
  var dispersionModifier: Int = 0,
  var fieldOfView: Int? = null,
  var sightDispersion: Int? = null,
  var aimSpeedModifier: Int = 0,
  var aimSpeed: Int? = null,
  var handlingModifier: Int = 0,
  var rangeModifier: Int = 0,
  var rangeMultiplier: Double = 1.0,
  var consumeChance: Int = 10000,
  var consumeDivisor: Int = 1,
  var shotSpreadMultiplierModifier: Double = 1.0,
  var ammoEffects: MutableSet<String>? = null,
  var upsChargesMultiplier: Double = 1.0,
  var upsChargesModifier: Int = 0,
  var ammoToFireMultiplier: Double = 1.0,
  var ammoToFireModifier: Int = 0,
  var weightMultiplier: Double = 1.0,
  var overwriteMinCycleRecoil: Int? = null,
  @MapInfo(param = "S")
  var installTime: Time? = null,
  var modTargets: MutableSet<String> = mutableSetOf(),
  var modeModifier: MutableList<GunData.GunModeData> = mutableListOf(),
  var reloadModifier: Int = 0,
  var minStrRequiredMod: Int = 0,
  var addMod: MutableMap<Translation, Int> = mutableMapOf(),
  var blacklistMod: MutableSet<Translation> = mutableSetOf(),
) : CddaSubObject() {
  override fun finalize(jsonValue: Any, param: String) {
    if (this.aimSpeed != null && this.fieldOfView == null) {
      if (this.aimSpeed!! > 6) {
        this.aimSpeedModifier = 5 * (this.aimSpeed!! - 6)
        this.fieldOfView = 480;
      } else {
        this.fieldOfView = 480 - 30 * (6 - this.aimSpeed!!)
      }
    }
  }
}
