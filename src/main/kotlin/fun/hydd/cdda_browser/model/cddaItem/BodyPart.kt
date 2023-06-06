package `fun`.hydd.cdda_browser.model.cddaItem

import com.fasterxml.jackson.annotation.JsonInclude
import `fun`.hydd.cdda_browser.annotation.CddaItem
import `fun`.hydd.cdda_browser.annotation.CddaProperty
import `fun`.hydd.cdda_browser.model.FinalResult
import `fun`.hydd.cdda_browser.model.ModOrder
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemData
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemParser
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

@JsonInclude(JsonInclude.Include.NON_NULL)
class BodyPart : CddaItemData() {
  lateinit var id: String
  lateinit var name: Translation
  var nameMultiple: Translation? = null
  var accusative: Translation? = null
  var accusativeMultiple: Translation? = null
  lateinit var heading: Translation
  lateinit var headingMultiple: Translation
  var hpBarUiText: Translation? = null
  lateinit var encumbranceText: Translation
  var hitSize: Double = 0.0
  var hitDifficulty: Double = 0.0
  var baseHp: Double = 0.0

  lateinit var flags: MutableList<CddaItemRef>

  class Parser : CddaItemParser() {

    override fun parse(jsonEntity: Any, dependencies: MutableMap<CddaItemRef, ModOrder>): FinalResult {
      if (jsonEntity is JsonEntity) {
        val finalItem = BodyPart()
        finalItem.id = jsonEntity.id
        finalItem.name = jsonEntity.name
        finalItem.nameMultiple = jsonEntity.nameMultiple
        finalItem.accusative = jsonEntity.accusative
        finalItem.accusativeMultiple = jsonEntity.accusativeMultiple
        finalItem.heading = jsonEntity.heading
        finalItem.headingMultiple = jsonEntity.headingMultiple
        finalItem.hpBarUiText = jsonEntity.hpBarUiText
        finalItem.encumbranceText = jsonEntity.encumbranceText
        finalItem.hitSize = jsonEntity.hitSize
        finalItem.hitDifficulty = jsonEntity.hitDifficulty
        finalItem.baseHp = jsonEntity.baseHp
        finalItem.flags = jsonEntity.flags
        return FinalResult(finalItem, dependencies, null)
      } else {
        throw Exception("class not match, class is ${jsonEntity::class}")
      }
    }
  }

  @CddaItem
  class JsonEntity() {
    lateinit var id: String
    lateinit var name: Translation
    var nameMultiple: Translation? = null
    var accusative: Translation? = null
    var accusativeMultiple: Translation? = null
    lateinit var heading: Translation
    lateinit var headingMultiple: Translation
    var hpBarUiText: Translation? = null
    lateinit var encumbranceText: Translation
    var hitSize: Double = 0.0
    var hitDifficulty: Double = 0.0
    var baseHp: Double = 60.0
    //TODO
//    var statHpMods: StatHpMod? = null
//    var healBonus: Double = 0.0
//    var mendRate: Double = 1.0
//    var drenchCapacity: Double = 0.0
//    var drenchIncrement: Double = 2.0
//    var dryingChance: Double = 1.0
//    var dryingIncrement: Double = 1.0
//    var wetMorale: Double = 0.0
//    var ugliness: Double = 0.0
//    var uglinessMandatory: Double = 0.0
//    var isLimb: Boolean = false
//    var isVital: Boolean = false
//    var encumbImpactsDodge: Boolean = false
//    var limbTypes: MutableList<LimbType> = mutableListOf()
//    var limbType: String? = null


    @CddaProperty(para = "JSON_FLAG")
    var flags: MutableList<CddaItemRef> = mutableListOf()
  }

  data class LimbType(
    val name: String,
    val value: Double,
  ) {
    class JsonParser() : `fun`.hydd.cdda_browser.model.jsonParser.JsonParser<LimbType>() {
      override fun parse(jsonValue: Any, para: String): LimbType {
        if (jsonValue is JsonArray) {
          var name: String? = null
          var value: Double? = null
          jsonValue.forEachIndexed { index, it ->
            when (index) {
              0 -> name = it as String
              1 -> value = it as Double
              else -> throw Exception()
            }
          }
          if (name != null && value != null) return LimbType(name!!, value!!) else throw Exception()
        } else {
          throw IllegalArgumentException()
        }
      }
    }
  }

  data class StatHpMod(
    var strMod: Double = 3.0,
    var dexMod: Double = 0.0,
    var intMod: Double = 0.0,
    var perMod: Double = 0.0,
    var healthMod: Double = 0.0,
  ) {
    class JsonParser() : `fun`.hydd.cdda_browser.model.jsonParser.JsonParser<StatHpMod>() {
      override fun parse(jsonValue: Any, para: String): StatHpMod {
        if (jsonValue is JsonObject) {
          val result = StatHpMod()
          if (jsonValue.containsKey("str_mod")) result.strMod = jsonValue.getDouble("str_mod")
          if (jsonValue.containsKey("dex_mod")) result.dexMod = jsonValue.getDouble("dex_mod")
          if (jsonValue.containsKey("int_mod")) result.intMod = jsonValue.getDouble("int_mod")
          if (jsonValue.containsKey("per_mod")) result.perMod = jsonValue.getDouble("per_mod")
          if (jsonValue.containsKey("health_mod")) result.healthMod = jsonValue.getDouble("health_mod")
          return result
        } else {
          throw IllegalArgumentException()
        }
      }
    }
  }
}
