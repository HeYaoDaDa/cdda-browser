package `fun`.hydd.cdda_browser.model.cddaItem

import com.fasterxml.jackson.annotation.JsonInclude
import `fun`.hydd.cdda_browser.annotation.CddaItem
import `fun`.hydd.cdda_browser.model.FinalResult
import `fun`.hydd.cdda_browser.model.ModOrder
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemData
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemParser

@JsonInclude(JsonInclude.Include.NON_NULL)
class ModInfo : CddaItemData() {
  lateinit var id: String
  lateinit var name: Translation
  lateinit var authors: MutableSet<String>
  lateinit var maintainers: MutableSet<String>
  lateinit var description: Translation
  var version: String? = null
  lateinit var dependencies: MutableSet<String>
  var core: Boolean = false
  var obsolete: Boolean = false
  var category: Translation = Translation("NO CATEGORY")

  class Parser : CddaItemParser() {
    override fun parse(jsonEntity: Any, dependencies: MutableMap<CddaItemRef, ModOrder>): FinalResult {
      if (jsonEntity is JsonEntity) {
        val finalItem = ModInfo()
        finalItem.id = jsonEntity.id
        finalItem.name = jsonEntity.name
        finalItem.description = jsonEntity.description
        finalItem.authors = jsonEntity.authors.toMutableSet()
        finalItem.maintainers = jsonEntity.maintainers.toMutableSet()
        finalItem.version = jsonEntity.version
        finalItem.dependencies = jsonEntity.dependencies.toMutableSet()
        finalItem.core = jsonEntity.core
        finalItem.obsolete = jsonEntity.obsolete
        finalItem.category = getModCategory(jsonEntity.category)
        finalItem.itemName = finalItem.name
        finalItem.description = finalItem.description
        return FinalResult(finalItem, dependencies, null)
      } else {
        throw Exception("class not match, class is ${jsonEntity::class}")
      }
    }

    /**
     * Convert mod info category id to message
     *
     * @param value category id
     * @return category message
     */
    private fun getModCategory(value: String): Translation {
      val message = when (value) {
        "total_conversion" -> "TOTAL CONVERSIONS"
        "content" -> "CORE CONTENT PACKS"
        "items" -> "ITEM ADDITION MODS"
        "creatures" -> "CREATURE MODS"
        "misc_additions" -> "MISC ADDITIONS"
        "buildings" -> "BUILDINGS MODS"
        "vehicles" -> "VEHICLE MODS"
        "rebalance" -> "REBALANCING MODS"
        "magical" -> "MAGICAL MODS"
        "item_exclude" -> "ITEM EXCLUSION MODS"
        "monster_exclude" -> "MONSTER EXCLUSION MODS"
        "graphical" -> "GRAPHICAL MODS"
        else -> "NO CATEGORY"
      }
      return Translation(message)
    }
  }

  @CddaItem
  class JsonEntity() {
    lateinit var id: String
    lateinit var name: Translation
    var authors: MutableList<String> = mutableListOf()
    var maintainers: MutableList<String> = mutableListOf()
    lateinit var description: Translation
    var version: String? = null
    var dependencies: MutableList<String> = mutableListOf()
    var core: Boolean = false
    var obsolete: Boolean = false
    var category: String = "NO CATEGORY"
  }
}
