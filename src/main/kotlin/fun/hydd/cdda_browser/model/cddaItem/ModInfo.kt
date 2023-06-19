package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation

class ModInfo : CddaObject() {
  lateinit var id: String
  lateinit var name: Translation
  var authors: MutableSet<String> = mutableSetOf()
  var maintainers: MutableSet<String> = mutableSetOf()
  lateinit var description: Translation
  var version: String? = null
  var dependencies: MutableSet<String> = mutableSetOf()
  var core: Boolean = false
  var obsolete: Boolean = false

  @MapInfo(key = "category")
  var jsonCategory: String = "NO CATEGORY"

  @MapInfo(ignore = true)
  var category: Translation = Translation("NO CATEGORY")

  override fun finalize(commonItem: CddaCommonItem, itemRef: CddaItemRef): CddaItemRef? {
    this.category = getModCategory(this.jsonCategory)
    this.itemName = this.name
    this.itemDescription = this.description
    return null
  }

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
