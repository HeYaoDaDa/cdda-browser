package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.DamageInstance

data class AmmoData(
  @MapInfo(param = "AMMUNITION_TYPE")
  var ammoType: CddaItemRef = CddaItemRef(),
  @MapInfo(param = "ITEM")
  var casing: CddaItemRef? = null,
  @MapInfo(param = "ITEM")
  var drop: CddaItemRef? = null,
  var dropChance: Double = 1.0,
  var dropActive: Boolean = true,
  var projectileCount: Int = 1,
  var shotSpread: Int = 0,
  var shotDamage: DamageInstance? = null,
  var damage: DamageInstance? = null,
  var range: Int = 0,
  var rangeMultiplier: Double = 1.0,
  var dispersion: Int = 0,
  var recoil: Int = 0,
  var count: Int = 1,
  var loudness: Int = 0,
  var effects: MutableList<String> = mutableListOf(),
  var criticalMultiplier: Double = 1.0,
  var showStats: Boolean = false
) : CddaSubObject()
