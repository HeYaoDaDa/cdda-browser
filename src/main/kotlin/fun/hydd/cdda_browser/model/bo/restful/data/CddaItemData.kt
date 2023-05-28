package `fun`.hydd.cdda_browser.model.bo.restful.data

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.model.entity.CddaItem
import io.vertx.core.json.JsonObject

data class CddaItemData(
  val jsonType: JsonType,
  val cddaType: CddaType,
  val modId: String,
  val id: String,
  val path: String,
  val json: JsonObject,
  val abstract: Boolean,
  val data: JsonObject,
  var name: Translation,
  var description: Translation?
) {
  companion object {
    fun of(cddaItem: CddaItem): CddaItemData {
      return CddaItemData(
        cddaItem.jsonType!!,
        cddaItem.cddaType!!,
        cddaItem.mod!!.modId!!,
        cddaItem.cddaId!!,
        cddaItem.path!!,
        cddaItem.originalJson!!.json!!,
        cddaItem.abstract!!,
        cddaItem.json!!.json!!,
        Translation.of(cddaItem.name!!),
        if (cddaItem.description != null) Translation.of(cddaItem.description!!) else null
      )
    }
  }
}
