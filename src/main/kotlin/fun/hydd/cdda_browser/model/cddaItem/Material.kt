package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.annotation.CddaItem
import `fun`.hydd.cdda_browser.annotation.CddaProperty
import `fun`.hydd.cdda_browser.model.FinalResult
import `fun`.hydd.cdda_browser.model.ModOrder
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemData
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemParser
import `fun`.hydd.cdda_browser.model.jsonParser.StrNumPair
import `fun`.hydd.cdda_browser.model.jsonParser.unit.Energy
import `fun`.hydd.cdda_browser.model.jsonParser.unit.Volume
import io.vertx.core.json.JsonObject

class Material : CddaItemData() {
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
  var breathability: String = "IMPERMEABLE"
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
  var salvagedInto: CddaItemRef? = null
  var repairedWith: CddaItemRef? = null

  class Parser : CddaItemParser() {
    override fun parse(jsonEntity: Any, dependencies: MutableMap<CddaItemRef, ModOrder>): FinalResult {
      if (jsonEntity is JsonEntity) {
        val result = Material()
        result.name = jsonEntity.name
        result.bashResist = jsonEntity.bashResist
        result.cutResist = jsonEntity.cutResist
        result.acidResist = jsonEntity.acidResist
        result.fireResist = jsonEntity.fireResist
        result.elecResist = jsonEntity.elecResist
        result.bulletResist = jsonEntity.bulletResist
        result.biologicResist = jsonEntity.biologicResist
        result.coldResist = jsonEntity.coldResist
        result.chipResist = jsonEntity.chipResist
        result.density = jsonEntity.density
        result.conductive = jsonEntity.conductive
        result.sheetThickness = jsonEntity.sheetThickness
        result.windResist = jsonEntity.windResist
        result.specificHeatLiquid = jsonEntity.specificHeatLiquid
        result.specificHeatSolid = jsonEntity.specificHeatSolid
        result.latentHeat = jsonEntity.latentHeat
        result.freezingPoint = jsonEntity.freezingPoint
        result.breathability = jsonEntity.breathability
        result.edible = jsonEntity.edible
        result.rotting = jsonEntity.rotting
        result.soft = jsonEntity.soft
        result.uncomfortable = jsonEntity.uncomfortable
        result.reinforces = jsonEntity.reinforces
        result.vitamins = jsonEntity.vitamins
        result.bashDmgVerb = jsonEntity.bashDmgVerb
        result.cutDmgVerb = jsonEntity.cutDmgVerb
        result.dmgAdj = jsonEntity.dmgAdj
        result.burnData = jsonEntity.burnData
        if (result.burnData.isEmpty()) {
          result.burnData.add(MatBurnData(burn = 1.0))
        }
        result.fuelData = jsonEntity.fuelData
        result.burnProducts = jsonEntity.burnProducts
        result.salvagedInto = jsonEntity.salvagedInto
        result.repairedWith = jsonEntity.repairedWith
        return FinalResult(result, dependencies, null)
      } else {
        throw Exception("class not match, class is ${jsonEntity::class}")
      }
    }

  }

  @CddaItem
  class JsonEntity() {
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
    var breathability: String = "IMPERMEABLE"
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


    @CddaProperty(para = "JSON_FLAG")//todo change to ITEM
    var salvagedInto: CddaItemRef? = null

    @CddaProperty(para = "JSON_FLAG")//todo change to ITEM
    var repairedWith: CddaItemRef? = null
  }

  data class MatBurnData(
    var immune: Boolean = false,
    var volumePerTurn: Volume = Volume(0),
    var fuel: Double = 0.0,
    var smoke: Double = 0.0,
    var burn: Double = 0.0,
  ) {
    class JsonParser() : `fun`.hydd.cdda_browser.model.jsonParser.JsonParser<MatBurnData>() {
      override fun parse(jsonValue: Any, para: String): MatBurnData {
        if (jsonValue is JsonObject) {
          val result = MatBurnData()
          if (jsonValue.containsKey("immune")) result.immune = jsonValue.getBoolean("immune")
          if (jsonValue.containsKey("volume_per_turn")) result.volumePerTurn =
            Volume.JsonParser().parse(jsonValue.getValue("volume_per_turn"), "")
          if (jsonValue.containsKey("fuel")) result.fuel = jsonValue.getDouble("fuel")
          if (jsonValue.containsKey("smoke")) result.smoke = jsonValue.getDouble("smoke")
          if (jsonValue.containsKey("burn")) result.burn = jsonValue.getDouble("burn")
          return result
        } else {
          throw IllegalArgumentException()
        }
      }
    }
  }

  data class FuelData(
    var energy: Energy = Energy(0),
    var explosionData: FuelExplosionData? = null,
    var pumpTerrain: String = "t_null",
    var isPerpetualFuel: Boolean = false,
  ) {
    class JsonParser() : `fun`.hydd.cdda_browser.model.jsonParser.JsonParser<FuelData>() {
      override fun parse(jsonValue: Any, para: String): FuelData {
        if (jsonValue is JsonObject) {
          val result = FuelData()
          result.energy = Energy.JsonParser().parse(jsonValue.getValue("energy"), "")
          if (jsonValue.containsKey("explosion_data")) result.explosionData =
            FuelExplosionData.JsonParser().parse(jsonValue.getValue("explosion_data"), "")
          if (jsonValue.containsKey("pump_terrain")) result.pumpTerrain = jsonValue.getString("pump_terrain")
          if (jsonValue.containsKey("perpetual")) result.isPerpetualFuel = jsonValue.getBoolean("perpetual")
          return result
        } else {
          throw IllegalArgumentException()
        }
      }
    }
  }

  data class FuelExplosionData(
    var explosionChanceHot: Double = 0.0,
    var explosionChanceCold: Double = 0.0,
    var explosionFactor: Double = 0.0,
    var fuelSizeFactor: Double = 0.0,
    var fieryExplosion: Boolean = false,
  ) {
    class JsonParser() : `fun`.hydd.cdda_browser.model.jsonParser.JsonParser<FuelExplosionData>() {
      override fun parse(jsonValue: Any, para: String): FuelExplosionData {
        if (jsonValue is JsonObject) {
          val result = FuelExplosionData()
          if (jsonValue.containsKey("chance_hot")) result.explosionChanceHot = jsonValue.getDouble("chance_hot")
          if (jsonValue.containsKey("chance_cold")) result.explosionChanceCold = jsonValue.getDouble("chance_cold")
          if (jsonValue.containsKey("factor")) result.explosionFactor = jsonValue.getDouble("factor")
          if (jsonValue.containsKey("size_factor")) result.fuelSizeFactor = jsonValue.getDouble("size_factor")
          if (jsonValue.containsKey("fiery")) result.fieryExplosion = jsonValue.getBoolean("fiery")
          return result
        } else {
          throw IllegalArgumentException()
        }
      }
    }
  }
}
