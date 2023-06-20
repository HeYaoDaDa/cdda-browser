package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Volume

data class PetArmorData(
  var materialThickness: Double = 0.0,
  @MapInfo(key = "max_pet_vol") var maxPetVolume: Volume = Volume(),
  @MapInfo(key = "min_pet_vol") var minPetVolume: Volume = Volume(),
  var petBodytype: String = "none",
  var environmentalProtectionWithFilter: Int = 0,
  var environmentalProtection: Int = 0,
  var powerArmor: Boolean = false
) : CddaSubObject()
