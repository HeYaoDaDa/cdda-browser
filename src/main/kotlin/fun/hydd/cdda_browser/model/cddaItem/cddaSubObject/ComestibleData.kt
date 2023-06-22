package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.model.base.ProcessContext
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.Material
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Time
import `fun`.hydd.cdda_browser.util.JsonUtil
import `fun`.hydd.cdda_browser.util.extension.getOrCreateJsonArray
import io.vertx.core.json.JsonObject

data class ComestibleData(
  var comestibleType: String = "",
  @MapInfo(param = "ITEM")
  var tool: CddaItemRef? = null,
  var charges: Int = 1,
  var quench: Int = 0,
  var `fun`: Int = 0,
  var stim: Int = 0,
  var fatigueMod: Int = 0,
  var healthy: Int = 0,
  var parasites: Int = 0,
  var radiation: Int = 0,
  var freezingPoint: Int = 0,
  var spoilsIn: Time = Time(),
  @MapInfo(param = "ITEM")
  var cooksLike: CddaItemRef? = null,
  @MapInfo(param = "ITEM")
  var smokingResult: CddaItemRef? = null,
  var petfood: MutableSet<String> = mutableSetOf(),
  @IgnoreMap
  @MapInfo(spFun = "contaminationFun")
  var contamination: MutableMap<CddaItemRef, Int> = mutableMapOf(),
  @MapInfo(param = "MATERIAL")
  var primaryMaterial: CddaItemRef? = null,
  @IgnoreMap
  @MapInfo(spFun = "materialFun")
  var material: MutableMap<CddaItemRef, Int>? = null,
  var monotonyPenalty: Int = 2,
  @MapInfo(param = "NULL")//todo change to addiction_type
  var addictionType: CddaItemRef? = null,
  var addictionPotential: Int = 0,
  var calories: Int? = null,
  @MapInfo(param = "nutrition")
  var nutritionJson: Int? = null,
  @IgnoreMap
  var nutrition: Nutrition = Nutrition(),
  var vitamins: MutableList<StrNumPair> = mutableListOf(),
  @MapInfo(param = "NULL")//todo change to mon_group
  var rotSpawn: CddaItemRef? = null,
  var rotSpawnChance: Int = 0,
  @IgnoreMap
  var specificHeatSolid: Double = 2.108,
  @IgnoreMap
  var specificHeatLiquid: Double = 4.186,
  @IgnoreMap
  var latentHeat: Double = 333.0,
) : CddaSubObject() {
  fun contaminationFun(jsonObject: JsonObject) {
    jsonObject.getOrCreateJsonArray("contamination")?.forEach {
      if (it is JsonObject) {
        //todo change to disease
        contamination[CddaItemRef(CddaType.NULL, it.getString("disease"))] = it.getInteger("probability")
      } else throw Exception("${it::class} is not JsonObject")
    }
  }

  fun materialFun(jsonObject: JsonObject) {
    if (jsonObject.containsKey("material")) this.material = mutableMapOf()
    jsonObject.getOrCreateJsonArray("material")?.forEach {
      when (it) {
        is JsonObject -> this.material!![CddaItemRef(CddaType.MATERIAL, it.getString("type"))] =
          it.getInteger("portion")

        is String -> this.material!![CddaItemRef(CddaType.MATERIAL, it)] = 1
        else -> throw Exception("${it::class} is not JsonObject")
      }
    }
  }

  override fun finalize(jsonValue: Any, param: String) {
    finalizeHeat()
    if (this.calories != null) {
      this.nutrition.calories = 1000 * this.calories!!
    }
    if (this.nutritionJson != null) {
      this.nutrition.calories = (1000 * this.nutritionJson!! * (2500.0f / (12 * 24))).toInt()
    }
    this.vitamins.forEach {
      //todo change to vitamin
      this.nutrition.vitamins[CddaItemRef(CddaType.NULL, it.name)] = it.value.toInt()
    }
  }

  private fun finalizeHeat() {
    if (this.primaryMaterial != null) {
      val pMaterial = ProcessContext.finalManager.find(this.primaryMaterial!!).cddaObject as Material
      this.specificHeatSolid = pMaterial.specificHeatSolid
      this.specificHeatLiquid = pMaterial.specificHeatLiquid
      this.latentHeat = pMaterial.latentHeat
    } else if (this.material != null) {
      var solid = 0.0
      var liquid = 0.0
      var latent = 0.0
      var materialTotal = 0
      this.material!!.forEach {
        val material = ProcessContext.finalManager.find(it.key).cddaObject as Material
        solid += material.specificHeatSolid
        liquid += material.specificHeatLiquid
        latent += material.latentHeat
        materialTotal += it.value
      }
      this.specificHeatSolid = JsonUtil.formatDouble(solid / materialTotal)
      this.specificHeatLiquid = JsonUtil.formatDouble(liquid / materialTotal)
      this.latentHeat = JsonUtil.formatDouble(latent / materialTotal)
    }
  }
}
