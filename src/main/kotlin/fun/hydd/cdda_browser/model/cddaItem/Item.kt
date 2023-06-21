package `fun`.hydd.cdda_browser.model.cddaItem

import com.fasterxml.jackson.annotation.JsonIgnore
import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.model.base.ProcessContext
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.*
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Length
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Money
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Volume
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Weight
import `fun`.hydd.cdda_browser.util.JsonUtil
import `fun`.hydd.cdda_browser.util.extension.getOrCreateJsonArray
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import kotlin.math.max

class Item : CddaObject() {
  lateinit var name: Translation
  var description: Translation? = null
  var symbol: String? = null
  var color: String? = null

  @MapInfo(param = "ITEM_CATEGORY")
  var category: CddaItemRef? = null
  var weight: Weight = Weight(0)
  var integralWeight: Weight = Weight(-1)
  var volume: Volume = Volume(0)
  var longestSide: Length = Length(-1)
  var price: Money = Money(0)
  var pricePostapoc: Money = Money(0)
  var stackable: Boolean = false
  var integralVolume: Volume = Volume(-1)
  var integralLongestSide: Length = Length(0)
  var toHit: ToHit = ToHit()
  var variants: MutableList<VariantData> = mutableListOf()
  var sealed: Boolean = false
  var minStrength: Int = 0
  var minDexterity: Int = 0
  var minIntelligence: Int = 0
  var minPerception: Int = 0
  var explodeInFire: Boolean = false
  var insulation: Double = 1.0
  var solarEfficiency: Double = 0.0
  var explosion: ExplosionData? = null
  var qualities: MutableList<QualityData> = mutableListOf()

  //todo use_action,countdown_action,drop_action
  var countdownInterval: Int = 0
  var countdownDestroy: Boolean = false
  var pocketData: MutableList<PocketData> = mutableListOf()

  @MapInfo(spFun = "ammoDataFun")
  var ammoData: AmmoData? = null

  @MapInfo(spFun = "gunDataFun")
  var gunData: GunData? = null

  @MapInfo(spFun = "armorDataFun")
  var armorData: ArmorData? = null

  @MapInfo(spFun = "petArmorDataFun")
  var petArmorData: PetArmorData? = null

  @IgnoreMap
  @MapInfo(spFun = "toolDataFun")
  var toolData: ToolData? = null

  @IgnoreMap
  @MapInfo(spFun = "toolModDataFun")
  var toolModData: ToolModData? = null

  @MapInfo(spFun = "bookDataFun")
  var bookData: BookData? = null

  @IgnoreMap
  @MapInfo(spFun = "conditionalNamesFun")
  var conditionalNames: MutableList<Translation> = mutableListOf()

  @MapInfo(param = "ITEM")
  var looksLike: CddaItemRef? = null

  @MapInfo(param = "NULL")//todo change to technique
  var techniques: MutableList<CddaItemRef> = mutableListOf()

  @IgnoreMap
  var properties: MutableMap<String, String> = mutableMapOf()

  @MapInfo(key = "properties", spFun = "propertiesFun")
  var propertiesJson: MutableList<StrStrPair> = mutableListOf()

  @IgnoreMap
  var chargedQualities: MutableMap<CddaItemRef, Int> = mutableMapOf()

  @JsonIgnore
  @MapInfo(key = "charged_qualities", spFun = "chargedQualitiesFun")
  var chargedQualitiesJson: MutableList<StrNumPair> = mutableListOf()

  @MapInfo(param = "NULL")//todo change to fault
  var faults: MutableList<CddaItemRef> = mutableListOf()

  @MapInfo(param = "JSON_FLAG")
  var flags: MutableList<CddaItemRef> = mutableListOf()

  @IgnoreMap
  var minSkills: MutableMap<CddaItemRef, Int> = mutableMapOf()

  @JsonIgnore
  @MapInfo(key = "min_skills", spFun = "minSkillsFun")
  var minSkillsJson: MutableList<StrNumPair> = mutableListOf()

  @MapInfo(param = "NULL")//todo change to requirement
  var templateRequirements: CddaItemRef? = null

  @MapInfo(param = "NULL")//todo change to item_group
  var nanofabTemplateGroup: CddaItemRef? = null

  @IgnoreMap
  @MapInfo(spFun = "magazinesFun")
  var magazines: MutableMap<String, MutableList<CddaItemRef>> = mutableMapOf()

  @IgnoreMap
  var defaultMagazine: MutableMap<String, CddaItemRef> = mutableMapOf()

  var phase: PhaseType = PhaseType.SOLID

  @IgnoreMap
  @MapInfo(spFun = "materialsFun")
  var materials: MutableList<Pair<CddaItemRef, Int>> = mutableListOf()

  @IgnoreMap
  var defaultMaterial: CddaItemRef? = null

  @JsonIgnore
  @MapInfo(spFun = "degradationMultiplierFun")
  var degradationMultiplier: Double = 1.0

  @JsonIgnore
  @MapInfo(spFun = "damageStatesFun")
  var damageStates: MutableList<Int> = mutableListOf()

  @MapInfo(param = "WEAPON_CATEGORY")
  var weaponCategory: MutableList<CddaItemRef> = mutableListOf()

  @MapInfo(param = "ITEM")
  var repairsLike: CddaItemRef? = null

  @MapInfo(spFun = "thrownDamageFun")
  var thrownDamage: DamageInstance = DamageInstance()

  @MapInfo(param = "MATERIAL")
  var repairsWith: MutableList<CddaItemRef> = mutableListOf()

  @MapInfo(param = "ASCII_ART")
  var asciiPicture: CddaItemRef? = null

  @MapInfo(param = "NULL")//todo change to emit
  var emits: MutableList<CddaItemRef> = mutableListOf()

  @MapInfo(param = "ITEM")
  var container: CddaItemRef? = null

  @JsonIgnore
  @MapInfo(spFun = "bashingFun")
  var bashing: Int = 0

  @JsonIgnore
  @MapInfo(spFun = "cuttingFun")
  var cutting: Int = 0

  @IgnoreMap
  var melee: DamageInstance = DamageInstance()

  @IgnoreMap
  var damageMin: Int = -1000

  @IgnoreMap
  var damageMax: Int = 4000

  @IgnoreMap
  var degradeIncrements: Int = 50

  fun ammoDataFun() {
    if (ProcessContext.commonItem!!.jsonType == JsonType.AMMO) {
      this.ammoData = AmmoData()
      this.ammoData!!.parse(ProcessContext.commonItem!!.json, "")
    }
  }

  fun gunDataFun() {
    if (ProcessContext.commonItem!!.jsonType == JsonType.GUN) {
      this.gunData = GunData()
      this.gunData!!.parse(ProcessContext.commonItem!!.json, "")
    }
  }

  fun armorDataFun() {
    if (listOf(JsonType.ARMOR, JsonType.TOOL_ARMOR).contains(ProcessContext.commonItem!!.jsonType)) {
      this.armorData = ArmorData()
      this.armorData!!.parse(ProcessContext.commonItem!!.json, "")
    }
  }

  fun petArmorDataFun() {
    if (ProcessContext.commonItem!!.jsonType == JsonType.PET_ARMOR) {
      this.petArmorData = PetArmorData()
      this.petArmorData!!.parse(ProcessContext.commonItem!!.json, "")
    }
  }

  fun toolDataFun() {
    if (ProcessContext.commonItem!!.jsonType == JsonType.TOOL) {
      this.toolData = ToolData()
      this.toolData!!.parse(ProcessContext.commonItem!!.json, "")
    }
  }

  fun toolModDataFun() {
    if (ProcessContext.commonItem!!.jsonType == JsonType.TOOLMOD) {
      this.toolModData = ToolModData()
      this.toolModData!!.parse(ProcessContext.commonItem!!.json, "")
    }
  }

  fun bookDataFun() {
    if (ProcessContext.commonItem!!.jsonType == JsonType.BOOK) {
      this.bookData = BookData()
      this.bookData!!.parse(ProcessContext.commonItem!!.json, "")
    }
  }

  fun conditionalNamesFun() {
    ProcessContext.commonItem!!.json.getOrCreateJsonArray("conditionalNamesFun")?.forEach { jsonObject ->
      if (jsonObject is JsonObject) {
        this.conditionalNames.add(Translation(jsonObject.getString("name")))
      }
    }
  }

  fun propertiesFun() {
    this.propertiesJson.forEach {
      this.properties[it.name] = it.value
    }
  }

  fun chargedQualitiesFun() {
    this.chargedQualitiesJson.forEach {
      //todo change to quality
      this.chargedQualities[CddaItemRef(CddaType.NULL, it.name)] = it.value.toInt()
    }
  }

  fun minSkillsFun() {
    this.minSkillsJson.forEach {
      //todo change to skill
      this.minSkills[CddaItemRef(CddaType.NULL, it.name)] = it.value.toInt()
    }
  }

  fun magazinesFun() {
    ProcessContext.commonItem!!.json.getOrCreateJsonArray("magazines")?.forEach {
      if (it is JsonArray) {
        val ammoType = it.list[0] as String
        val magazines = mutableListOf<CddaItemRef>()
        val magazineJson = it.list[1]
        if (magazineJson is ArrayList<*>) {
          magazineJson.forEach { magazineStr ->
            magazines.add(CddaItemRef(CddaType.ITEM, magazineStr as String))
          }
        } else throw Exception()
        this.magazines[ammoType] = magazines
      } else throw Exception()
    }
    this.magazines.forEach {
      this.defaultMagazine[it.key] = it.value.first()
    }
  }

  fun materialsFun() {
    ProcessContext.commonItem!!.json.getOrCreateJsonArray("material")?.forEachIndexed { index, any ->
      when (any) {
        is JsonObject -> {
          val material = CddaItemRef(CddaType.MATERIAL, any.getString("type"))
          this.materials.add(
            Pair(
              material,
              any.getInteger("portion") ?: 1
            )
          )
          if (index == 0) this.defaultMaterial = material
        }

        is String -> {
          val material = CddaItemRef(CddaType.MATERIAL, any)
          this.materials.add(Pair(material, 1))
          if (index == 0) this.defaultMaterial = material
        }
      }
    }
  }

  fun degradationMultiplierFun() {
    if (this.category?.id != "veh_parts") {
      this.degradationMultiplier = 0.0
    }
    if (this.degradationMultiplier <= 1.0f / ((this.damageMax - this.damageMin) * 2.0f)) {
      this.degradeIncrements = 0
    } else {
      if (this.degradationMultiplier != 0.0) {
        this.degradeIncrements = max(this.degradeIncrements / this.degradationMultiplier, 1.0).toInt()
      } else {
        this.degradeIncrements = 0
      }
    }
  }

  fun damageStatesFun() {
    if (this.damageStates.isNotEmpty()) {
      this.damageMin = this.damageStates[0] * 1000
      this.damageMax = this.damageStates[1] * 1000
    }
  }

  fun bashingFun() {
    this.melee.damageUnits.add(DamageInstance.DamageUnit(DamageInstance.DamageType.BASH, this.bashing.toDouble()))
  }

  fun cuttingFun() {
    this.melee.damageUnits.add(DamageInstance.DamageUnit(DamageInstance.DamageType.CUT, this.cutting.toDouble()))
  }

  fun thrownDamageFun() {
    if (this.thrownDamage.damageUnits.isEmpty()) {
      val dash = this.melee.damageUnits.find { it.damageType == DamageInstance.DamageType.BASH }?.amount ?: 0.0
      this.thrownDamage.damageUnits.add(
        DamageInstance.DamageUnit(
          DamageInstance.DamageType.BASH,
          dash + JsonUtil.formatDouble(this.weight.value / 1000.0)
        )
      )
    }
  }

  enum class PhaseType {
    PNULL,
    SOLID,
    LIQUID,
    GAS,
    PLASMA
  }

  data class VariantData(
    var id: String = "",
    var name: Translation = Translation(),
    var description: Translation = Translation(),
    var symbol: String? = null,
    var color: String? = null,
    @MapInfo(param = "ASCII_ART")
    var asciiPicture: CddaItemRef? = null,
    var weight: Int = 0,
    var append: Boolean = false,
  ) : CddaSubObject()

  enum class ToHitGrip(val value: Int) {
    BAD(0), NONE(1), SOLID(2), WEAPON(3)
  }

  enum class ToHitLength(val value: Int) {
    HAND(0), SHORT(1), LONG(2)
  }

  enum class ToHitSurface(val value: Int) {
    POINT(0), LINE(1), ANY(2), EVERY(3)
  }

  enum class ToHitBalance(val value: Int) {
    CLUMSY(0), UNEVEN(1), NEUTRAL(2), GOOD(3)
  }

  data class ToHit(@IgnoreMap var value: Int = 0) : CddaSubObject() {
    override fun finalize(jsonValue: Any, param: String) {
      when (jsonValue) {
        is Int -> {
          this.value = jsonValue
        }

        is JsonObject -> {
          val grip = ToHitGrip.valueOf((jsonValue.getString("grip") ?: "WEAPON").uppercase())
          val length = ToHitLength.valueOf((jsonValue.getString("length") ?: "HAND").uppercase())
          val surface = ToHitSurface.valueOf((jsonValue.getString("surface") ?: "ANY").uppercase())
          val balance = ToHitBalance.valueOf((jsonValue.getString("balance") ?: "NEUTRAL").uppercase())
          this.value = -7 + grip.value + length.value + surface.value + balance.value
        }

        else -> throw Exception("ToHit is not Int or JsonObject, it is ${jsonValue::class}")
      }
    }

    override fun relative(relativeJson: Any, param: String) {
      val relative = ToHit()
      relative.parse(relativeJson, param)
      this.value += relative.value
    }

    override fun proportional(proportionalJson: Any, param: String) {
      if (proportionalJson is Double) {
        this.value = (this.value * proportionalJson).toInt()
      } else throw Exception("ToHit proportional is not Double, it is $proportionalJson")
    }
  }


  data class QualityData(
    @MapInfo(param = "NULL")//todo change to quality
    var id: CddaItemRef = CddaItemRef(),
    var level: Int = 0
  ) : CddaSubObject()
}
