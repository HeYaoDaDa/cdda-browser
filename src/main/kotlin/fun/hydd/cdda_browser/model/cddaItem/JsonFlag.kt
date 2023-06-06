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

@JsonInclude(JsonInclude.Include.NON_NULL)
class JsonFlag : CddaItemData() {
  lateinit var id: String
  var info: Translation? = null
  lateinit var conflicts: MutableList<CddaItemRef>
  var inherit: Boolean = true
  var craftInherit: Boolean = false
  var requiresFlag: CddaItemRef? = null
  var tasteMod: Double? = null
  var restriction: Translation? = null
  var name: Translation? = null

  class Parser : CddaItemParser() {
    override fun parse(jsonEntity: Any, dependencies: MutableMap<CddaItemRef, ModOrder>): FinalResult {
      if (jsonEntity is JsonEntity) {
        val finalItem = JsonFlag()
        finalItem.id = jsonEntity.id
        finalItem.info = jsonEntity.info
        finalItem.conflicts = jsonEntity.conflicts
        finalItem.inherit = jsonEntity.inherit
        finalItem.craftInherit = jsonEntity.craftInherit
        finalItem.requiresFlag = jsonEntity.requiresFlag
        finalItem.tasteMod = jsonEntity.tasteMod
        finalItem.restriction = jsonEntity.restriction
        finalItem.name = jsonEntity.name
        return FinalResult(finalItem, dependencies, null)
      } else {
        throw Exception("class not match, class is ${jsonEntity::class}")
      }
    }
  }

  @CddaItem
  class JsonEntity() {
    lateinit var id: String
    var info: Translation? = null
    var inherit: Boolean = true
    var craftInherit: Boolean = false
    var tasteMod: Double? = null
    var restriction: Translation? = null
    var name: Translation? = null

    @CddaProperty(para = "JSON_FLAG")
    var conflicts: MutableList<CddaItemRef> = mutableListOf()

    @CddaProperty(para = "JSON_FLAG")
    var requiresFlag: CddaItemRef? = null
  }
}
