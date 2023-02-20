package `fun`.hydd.cdda_browser.model.cddaItem

import com.fasterxml.jackson.annotation.JsonInclude
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemData
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemParser
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseItem

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

    override fun doParse(item: CddaParseItem, data: CddaItemData, parent: Boolean): CddaItemRef? {
      if (data is ModInfo) {
        data.id = item.id
        data.name = item.getTranslation("name", null, if (parent) data.name else null) ?: throw Throwable("miss field")
        data.description = item.getTranslation("description", null, if (parent) data.description else null)
          ?: throw Throwable("miss field")
        data.authors = item.getCollection("authors", if (parent) data.authors else null, mutableSetOf()).toMutableSet()
        data.maintainers =
          item.getCollection("maintainers", if (parent) data.maintainers else null, mutableSetOf()).toMutableSet()
        data.version = item.getString("version", data.version)
        data.dependencies =
          item.getCollection("dependencies", if (parent) data.dependencies else null, mutableSetOf()).toMutableSet()
        data.core = item.getBoolean("core", data.core, false)
        data.obsolete = item.getBoolean("obsolete", data.obsolete, false)
        data.category = getModCategory(item.getString("category", data.category.value, ""))
        item.description = data.description
      } else throw IllegalArgumentException()
      return null
    }

    override fun getName(item: CddaParseItem, data: CddaItemData): Translation {
      return if (data is ModInfo) data.name else super.getName(item, data)
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
