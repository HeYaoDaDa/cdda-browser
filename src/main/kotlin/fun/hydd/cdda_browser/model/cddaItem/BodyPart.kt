package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.base.ProcessContext
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation
import `fun`.hydd.cdda_browser.util.extension.getOrCreateJsonArray
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

@Suppress("unused", "MemberVisibilityCanBePrivate")
class BodyPart : CddaObject() {
  lateinit var name: Translation
  var hitSize: Double = 0.0
  var hitDifficulty: Double = 0.0
  var baseHp: Int = 60
  var healBonus: Int = 0
  var mendRate: Double = 1.0
  var drenchCapacity: Int = 0
  var drenchIncrement: Int = 2
  var dryingChance: Int = 0
  var dryingIncrement: Int = 1
  var wetMorale: Int = 0
  var ugliness: Int = 0
  var uglinessMandatory: Int = 0
  var isLimb: Boolean = false
  var isVital: Boolean = false
  var encumbImpactsDodge: Boolean = false

  @IgnoreMap
  @MapInfo(spFun = "limbTypesFun")
  var limbTypes: MutableMap<CddaItemRef, Double> = mutableMapOf()

  @IgnoreMap
  var primaryLimbType: CddaItemRef = CddaItemRef()

  var envProtection: Int = 0
  var fireWarmthBonus: Int = 0

  @MapInfo(spFun = "mainPartFun")
  lateinit var mainPart: CddaItemRef
  lateinit var connectedTo: CddaItemRef
  lateinit var oppositePart: CddaItemRef
  var smashEfficiency: Double = 0.5
  var hotMoraleMod: Double = 0.0
  var coldMoraleMod: Double = 0.0
  var feelsDiscomfort: Boolean = true
  var stylishBonus: Double = 0.0
  var squeamishPenalty: Int = 0
  var bionicSlots: Int = 0

  @MapInfo(param = "JSON_FLAG")
  var flags: MutableList<CddaItemRef> = mutableListOf()

  @MapInfo(param = "JSON_FLAG")
  var conditionalFlags: MutableList<CddaItemRef> = mutableListOf()

  var encumbranceThreshold: Int = 0
  var encumbranceLimit: Int = 100
  var healthLimit: Int = 1




  @MapInfo(param = "SUB_BODY_PART")
  var subParts: MutableList<CddaItemRef> = mutableListOf()
  fun limbTypesFun(json: JsonObject) {
    if (json.containsKey("limb_types")) {
      var firstType: CddaItemRef? = null
      var setFirst = true
      json.getOrCreateJsonArray("limb_types")?.forEach {
        val type: CddaItemRef
        when (it) {
          is JsonArray -> {
            type = CddaItemRef(CddaType.BODY_PART, it.list[0] as String)
            this.limbTypes[type] = it.list[1] as Double
            setFirst = false
          }

          is String -> {
            type = CddaItemRef(CddaType.BODY_PART, it)
            this.limbTypes[type] = 1.0
          }

          else -> throw Exception("${it::class} is not JsonArray or String")
        }
        if (firstType == null) firstType = type
      }
      if (setFirst && firstType != null) this.primaryLimbType = firstType!!
    }
    if (json.containsKey("limb_type")) {
      this.limbTypes[CddaItemRef(CddaType.BODY_PART, json.getString("limb_type"))] = 1.0
    }
    if (this.primaryLimbType.type == CddaType.NULL) {
      this.primaryLimbType = this.limbTypes.maxBy { it.value }.key
    }
  }

  fun mainPartFun() {
    if (this.mainPart.id == ProcessContext.itemId) this.connectedTo = this.mainPart
  }

  override fun finalize(commonItem: CddaCommonItem, itemRef: CddaItemRef) {
    if (!commonItem.json.containsKey("drying_chance")) this.dryingChance = drenchCapacity
    this.itemName = this.name
  }
}
