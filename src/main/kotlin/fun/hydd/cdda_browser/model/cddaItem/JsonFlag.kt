package `fun`.hydd.cdda_browser.model.cddaItem

import com.fasterxml.jackson.annotation.JsonInclude
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemData
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemParser
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseItem

@JsonInclude(JsonInclude.Include.NON_NULL)
class JsonFlag : CddaItemData() {
  lateinit var id: String
  var info: Translation? = null
  lateinit var conflicts: MutableSet<CddaItemRef>
  var inherit: Boolean = true
  var craftInherit: Boolean = false
  var requiresFlag: CddaItemRef? = null
  var tasteMod: Double? = null
  var restriction: Translation? = null
  var name: Translation? = null

  class Parser : CddaItemParser() {
    override fun doParse(item: CddaParseItem, data: CddaItemData, parent: Boolean): CddaItemRef? {
      if (data is JsonFlag) {
        data.id = item.id
        data.info = item.getTranslation("info", null, data.name)
        data.conflicts =
          item.getCddaItemRefs(
            "conflicts",
            CddaType.JSON_FLAG,
            if (parent) data.conflicts.toSet() else null,
            mutableSetOf()
          ).toMutableSet()
        data.inherit = item.getBoolean("inherit", data.inherit, true)
        data.craftInherit = item.getBoolean("craft_inherit", data.craftInherit, false)
        data.craftInherit = item.getBoolean("craft_inherit", data.craftInherit, false)
        data.requiresFlag = item.getCddaItemRef("requires_flag", CddaType.JSON_FLAG, data.requiresFlag)
        data.tasteMod = item.getDouble("taste_mod", data.tasteMod)
        data.restriction = item.getTranslation("restriction", null, data.restriction)
        data.name = item.getTranslation("name", null, data.name)
        item.description = data.info
      } else throw IllegalArgumentException()
      return null
    }

    override fun getName(item: CddaParseItem, data: CddaItemData): Translation {
      return if(data is JsonFlag) data.name?:super.getName(item, data) else super.getName(item, data)
    }

    override fun newData(): CddaItemData {
      return JsonFlag()
    }
  }
}
