package `fun`.hydd.cdda_browser.model.cddaItem

import com.fasterxml.jackson.annotation.JsonInclude
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemData
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemParser
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseItem

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
  lateinit var flags: MutableSet<CddaItemRef>

  class Parser : CddaItemParser() {
    override fun doParse(item: CddaParseItem, data: CddaItemData, parent: Boolean): CddaItemRef? {
      if (data is BodyPart) {
        data.id = item.id
        data.name = item.getTranslation("name", null, if (parent) data.name else null) ?: throw Throwable("miss field")
        data.nameMultiple = item.getTranslation("name_multiple", null, data.nameMultiple)
        data.accusative = item.getTranslation("accusative", null, data.accusative)
        data.accusativeMultiple = item.getTranslation("accusative_multiple", null, data.accusativeMultiple)
        data.heading =
          item.getTranslation("heading", null, if (parent) data.heading else null) ?: throw Throwable("miss field")
        data.headingMultiple =
          item.getTranslation("heading_multiple", null, if (parent) data.headingMultiple else null)
            ?: throw Throwable("miss field")
        data.hpBarUiText = item.getTranslation("hp_bar_ui_text", null, data.hpBarUiText)
        data.encumbranceText =
          item.getTranslation("encumbrance_text", null, if (parent) data.encumbranceText else null)
            ?: throw Throwable("miss field")
        data.hitSize = item.getDouble("hit_size", data.hitSize) ?: 0.0
        data.hitDifficulty = item.getDouble("hit_difficulty", data.hitDifficulty) ?: 0.0
        data.baseHp = item.getDouble("base_hp", data.baseHp) ?: 60.0
        data.flags =
          item.getCddaItemRefs("flags", CddaType.JSON_FLAG, if (parent) data.flags.toSet() else null, mutableSetOf())
            .toMutableSet()
      } else throw IllegalArgumentException()
      return null
    }

    override fun newData(): CddaItemData {
      return BodyPart()
    }
  }
}
