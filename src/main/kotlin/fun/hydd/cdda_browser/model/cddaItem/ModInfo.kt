package `fun`.hydd.cdda_browser.model.cddaItem

import com.fasterxml.jackson.annotation.JsonInclude
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemData
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemParser
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseItem

@JsonInclude(JsonInclude.Include.NON_NULL)
class ModInfo : CddaItemData() {
  var id: String? = null
  var name: Translation? = null
  var authors: Set<String> = mutableSetOf()
  var maintainers: Set<String> = mutableSetOf()
  var description: Translation? = null
  var version: String? = null
  var dependencies: Set<String> = mutableSetOf()
  var core: Boolean = false
  var obsolete: Boolean = false
  var category: Translation = Translation("NO CATEGORY")

  class Parser : CddaItemParser() {

    override fun doParse(item: CddaParseItem, data: CddaItemData): CddaItemRef? {
      if (data is ModInfo) {
        data.id = item.id
        data.name = item.getTranslation("name", null, data.name)
        data.description = item.getTranslation("description", null, data.description)
        data.authors = item.getCollection("authors", data.authors, emptySet()).toSet()
        data.maintainers = item.getCollection("maintainers", data.maintainers, emptySet()).toSet()
        data.version = item.getString("version", data.version)
        data.dependencies = item.getCollection("dependencies", data.dependencies, emptySet()).toSet()
        data.core = item.getBoolean("core", data.core, false)
        data.obsolete = item.getBoolean("obsolete", data.obsolete, false)
        data.category = getModCategory(item.getString("category", data.category.value, ""))
      } else throw IllegalArgumentException()
      return null
    }

    override fun newData(): CddaItemData {
      return ModInfo()
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
}
