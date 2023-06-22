package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.util.JsonUtil
import io.vertx.core.json.JsonObject

data class DamageInstance(
  var damageUnits: MutableList<DamageUnit> = mutableListOf()
) : CddaSubObject() {
  override fun relative(relativeJson: Any, param: String) {
    if (relativeJson is JsonObject) {
      val damageType = DamageType.valueOf(relativeJson.getString("damage_type").uppercase())
      val exitsUnit = this.damageUnits.find { damageType == it.damageType }
      if (exitsUnit != null) {
        if (relativeJson.containsKey("amount"))
          exitsUnit.amount += relativeJson.getDouble("amount")
        if (relativeJson.containsKey("armor_penetration"))
          exitsUnit.armorPenetration += relativeJson.getDouble("armor_penetration")
        if (relativeJson.containsKey("armor_multiplier"))
          exitsUnit.armorMultiplier += relativeJson.getDouble("armor_multiplier")
        if (relativeJson.containsKey("damage_multiplier"))
          exitsUnit.damageMultiplier += relativeJson.getDouble("damage_multiplier")
        if (relativeJson.containsKey("constant_armor_multiplier"))
          exitsUnit.constantArmorMultiplier += relativeJson.getDouble("constant_armor_multiplier")
        if (relativeJson.containsKey("constant_damage_multiplier"))
          exitsUnit.constantDamageMultiplier += relativeJson.getDouble("constant_damage_multiplier")
      } else {
        val newUnit = DamageUnit()
        newUnit.parse(relativeJson, param)
        this.damageUnits.add(newUnit)
      }
    } else throw Exception("DamageInstance relativeJson class not is JsonObject, it is ${relativeJson::class}")
  }

  override fun proportional(proportionalJson: Any, param: String) {
    if (proportionalJson is JsonObject) {
      val damageType = DamageType.valueOf(proportionalJson.getString("damage_type").uppercase())
      val exitsUnit = this.damageUnits.find { damageType == it.damageType }
      if (exitsUnit != null) {
        if (proportionalJson.containsKey("amount"))
          exitsUnit.amount = JsonUtil.formatDouble(exitsUnit.amount * proportionalJson.getDouble("amount"))
        if (proportionalJson.containsKey("armor_penetration"))
          exitsUnit.armorPenetration =
            JsonUtil.formatDouble(exitsUnit.armorPenetration * proportionalJson.getDouble("armor_penetration"))
        if (proportionalJson.containsKey("armor_multiplier"))
          exitsUnit.armorMultiplier =
            JsonUtil.formatDouble(exitsUnit.armorMultiplier * proportionalJson.getDouble("armor_multiplier"))
        if (proportionalJson.containsKey("damage_multiplier"))
          exitsUnit.damageMultiplier =
            JsonUtil.formatDouble(exitsUnit.damageMultiplier * proportionalJson.getDouble("damage_multiplier"))
        if (proportionalJson.containsKey("constant_armor_multiplier"))
          exitsUnit.constantArmorMultiplier =
            JsonUtil.formatDouble(exitsUnit.constantArmorMultiplier * proportionalJson.getDouble("constant_armor_multiplier"))
        if (proportionalJson.containsKey("constant_damage_multiplier"))
          exitsUnit.constantDamageMultiplier =
            JsonUtil.formatDouble(exitsUnit.constantDamageMultiplier * proportionalJson.getDouble("constant_damage_multiplier"))
      }
    } else throw Exception("DamageInstance proportionalJson class not is JsonObject, it is ${proportionalJson::class}")
  }

  enum class DamageType {
    NULL,
    PURE,
    BIOLOGICAL,
    BASH,
    CUT,
    ACID,
    STAB,
    HEAT,
    COLD,
    ELECTRIC,
    BULLET,
  }

  data class DamageUnit(
    @IgnoreMap
    @MapInfo(spFun = "mapDamageType")
    var damageType: DamageType = DamageType.NULL,
    var amount: Double = 0.0,
    var armorPenetration: Double = 0.0,
    var armorMultiplier: Double = 1.0,
    var damageMultiplier: Double = 1.0,
    var constantArmorMultiplier: Double = 1.0,
    var constantDamageMultiplier: Double = 1.0,
  ) : CddaSubObject() {
    fun mapDamageType(jsonObject: JsonObject) {
      val damageStr = jsonObject.getString("damage_type")
      this.damageType = DamageType.valueOf(damageStr.uppercase())
    }
  }
}
