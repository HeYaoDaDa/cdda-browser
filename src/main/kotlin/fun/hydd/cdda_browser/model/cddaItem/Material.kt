package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.StrNumPair
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Energy
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Volume

class Material : CddaObject() {
  lateinit var id: String
  lateinit var name: Translation
  var bashResist: Double = 0.0
  var cutResist: Double = 0.0
  var acidResist: Double = 0.0
  var fireResist: Double = 0.0
  var elecResist: Double = 0.0
  var bulletResist: Double = 0.0
  var biologicResist: Double = 0.0
  var coldResist: Double = 0.0
  var chipResist: Double = 0.0
  var density: Double = 1.0
  var conductive: Boolean = false
  var sheetThickness: Double = 0.0
  var windResist: Double? = null
  var specificHeatLiquid: Double = 4.186
  var specificHeatSolid: Double = 2.108
  var latentHeat: Double = 334.0
  var freezingPoint: Double = 0.0
  var breathability: BreathabilityRate = BreathabilityRate.IMPERMEABLE
  var edible: Boolean = false
  var rotting: Boolean = false
  var soft: Boolean = false
  var uncomfortable: Boolean = false
  var reinforces: Boolean = false
  var vitamins: MutableList<StrNumPair> = mutableListOf()
  lateinit var bashDmgVerb: Translation
  lateinit var cutDmgVerb: Translation
  var dmgAdj: MutableList<Translation> = mutableListOf()
  var burnData: MutableList<MatBurnData> = mutableListOf()
  var fuelData: FuelData? = null
  var burnProducts: MutableList<StrNumPair> = mutableListOf()


  @MapInfo(param = "ITEM")
  var salvagedInto: CddaItemRef? = null

  @MapInfo(param = "ITEM")
  var repairedWith: CddaItemRef? = null

  override fun finalize(commonItem: CddaCommonItem, itemRef: CddaItemRef): CddaItemRef? {
    if (this.burnData.isEmpty()) {
      this.burnData.add(MatBurnData(burn = 1.0))
    }
    this.itemName = this.name
    return null
  }

  enum class BreathabilityRate {
    IMPERMEABLE,
    POOR,
    AVERAGE,
    GOOD,
    MOISTURE_WICKING,
    SECOND_SKIN,
  }

  data class MatBurnData(
    var immune: Boolean = false,
    var volumePerTurn: Volume = Volume(0),
    var fuel: Double = 0.0,
    var smoke: Double = 0.0,
    var burn: Double = 0.0,
  ) : CddaSubObject()

  data class FuelData(
    var energy: Energy = Energy(0),
    var explosionData: FuelExplosionData? = null,
    var pumpTerrain: String = "t_null",
    var isPerpetualFuel: Boolean = false,
  ) : CddaSubObject()

  data class FuelExplosionData(
    var explosionChanceHot: Double = 0.0,
    var explosionChanceCold: Double = 0.0,
    var explosionFactor: Double = 0.0,
    var fuelSizeFactor: Double = 0.0,
    var fieryExplosion: Boolean = false,
  ) : CddaSubObject()
}
