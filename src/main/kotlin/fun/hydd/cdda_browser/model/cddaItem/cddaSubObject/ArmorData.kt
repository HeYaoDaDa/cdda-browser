package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.model.base.ProcessContext
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.BodyPart
import `fun`.hydd.cdda_browser.model.cddaItem.Material
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Weight
import `fun`.hydd.cdda_browser.util.extension.getOrCreateJsonArray
import io.vertx.core.json.JsonObject

data class ArmorData(
  var armor: MutableList<ArmorPortionData> = mutableListOf(),
  @MapInfo(param = "BODY_PART")
  var covers: MutableSet<CddaItemRef>? = null,
  var materialThickness: Double? = null,
  var environmentalProtection: Int? = null,
  var environmentalProtectionWithFilter: Int? = null,
  var sided: Boolean = false,
  var warmth: Int = 0,
  @MapInfo(param = "ITEM")
  var nonFunctional: CddaItemRef? = null,
  var weightCapacityModifier: Double = 1.0,
  var weightCapacityBonus: Weight = Weight(),
  var powerArmor: Boolean = false,
  var validMods: MutableSet<String> = mutableSetOf()
) : CddaSubObject() {

  override fun finalize(jsonValue: Any, param: String) {
    this.armor.forEach {
      if (this.materialThickness != null) it.materialThickness = this.materialThickness!!
      if (this.environmentalProtection != null) it.environmentalProtection = this.environmentalProtection!!
      if (this.environmentalProtectionWithFilter != null) it.environmentalProtectionWithFilter =
        this.environmentalProtectionWithFilter!!
      if (this.covers != null)
        it.covers = this.covers!!
    }
  }

  enum class EncumbranceModifier {
    IMBALANCED,
    RESTRICTS_NECK,
    WELL_SUPPORTED,
    NONE
  }

  enum class LayerLevel {
    PERSONAL,
    SKINTIGHT,
    NORMAL,
    WAIST,
    OUTER,
    BELTED,
    AURA,
  };

  data class ArmorPortionData(
    @MapInfo(param = "BODY_PART")
    var covers: MutableSet<CddaItemRef> = mutableSetOf(),
    @MapInfo(param = "SUB_BODY_PART")
    var specificallyCovers: MutableSet<CddaItemRef> = mutableSetOf(),
    var coverage: Int = 0,
    @MapInfo(spFun = "coverMeleeFun")
    var coverMelee: Int? = null,
    @MapInfo(spFun = "coverageRangedFun")
    var coverageRanged: Int? = null,
    var coverageVitals: Int = 0,
    var breathability: Material.BreathabilityRate? = null,
    var rigidLayerOnly: Boolean = false,
    var encumbranceModifiers: MutableSet<EncumbranceModifier>? = null,
    @IgnoreMap
    @MapInfo(spFun = "encumbranceFun")
    var encumbrance: Int = 0,
    @IgnoreMap
    var maxEncumbrance: Int? = null,
    var materialThickness: Double = 0.0,
    var environmentalProtection: Int = 0,
    var environmentalProtectionWithFilter: Int = 0,
    var volumeEncumberModifier: Double = 1.0,
    @IgnoreMap
    @MapInfo(spFun = "materialFun")
    var material: MutableList<PartMaterial> = mutableListOf(),
    var layers: MutableList<LayerLevel> = mutableListOf()
  ) : CddaSubObject() {

    override fun finalize(jsonValue: Any, param: String) {
      if (this.covers.isEmpty() && this.specificallyCovers.isNotEmpty()) {
        this.covers.forEach {
          this.specificallyCovers.addAll((ProcessContext.finalManager.find(it).cddaObject as BodyPart).subParts)
        }
      }
    }

    fun coverMeleeFun(jsonValue: Any) {
      if (this.coverMelee == null) this.coverMelee = this.coverage
    }

    fun coverageRangedFun(jsonValue: Any) {
      if (this.coverageRanged == null) this.coverageRanged = this.coverage
    }

    fun encumbranceFun(jsonValue: Any) {
      if (jsonValue is JsonObject) {
        jsonValue.getOrCreateJsonArray("encumbrance")?.forEachIndexed { index, any ->
          when (index) {
            0 -> this.encumbrance = any as Int
            1 -> this.maxEncumbrance = any as Int
            else -> throw Exception("encumbrance size over 2")
          }
        }
      } else throw Exception("${jsonValue::class} is not JsonObject")
    }

    fun materialFun(jsonValue: Any) {
      if (jsonValue is JsonObject) {
        jsonValue.getOrCreateJsonArray("material")?.forEach {
          when (it) {
            is String -> this.material.add(PartMaterial(type = CddaItemRef(CddaType.MATERIAL, it)))
            is JsonObject -> {
              val temp = PartMaterial()
              temp.parse(it, "")
              this.material.add(temp)
            }

            else -> throw Exception("${it::class} is not JsonObject or String")
          }
        }
      } else throw Exception("${jsonValue::class} is not JsonObject")
    }
  }

  data class PartMaterial(
    @MapInfo(param = "Material")
    var type: CddaItemRef = CddaItemRef(),
    @MapInfo(key = "covered_by_mat")
    var cover: Int = 100,
    var thickness: Double = 0.0,
    var ignoreSheetThickness: Boolean = false,
  ) : CddaSubObject()
}
