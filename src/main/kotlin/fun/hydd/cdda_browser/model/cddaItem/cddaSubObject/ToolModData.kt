package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.util.extension.getOrCreateJsonArray
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

data class ToolModData(
  @MapInfo(param = "AMMUNITION_TYPE")
  var ammoModifier: MutableSet<CddaItemRef> = mutableSetOf(),
  @MapInfo(param = "AMMUNITION_TYPE")
  var acceptableAmmo: MutableSet<CddaItemRef> = mutableSetOf(),
  var capacityMultiplier: Double = 1.0,
  @IgnoreMap
  @MapInfo(spFun = "magazineAdaptorFun")
  var magazineAdaptor: MutableMap<CddaItemRef, MutableList<CddaItemRef>> = mutableMapOf(),
  var pocketMods: MutableList<PocketData> = mutableListOf()
) : CddaSubObject() {
  fun magazineAdaptorFun(jsonObject: JsonObject) {
    jsonObject.getOrCreateJsonArray("magazine_adaptor")?.forEach { any ->
      if (any is JsonArray) {
        val list = any.list
        this.magazineAdaptor[CddaItemRef(CddaType.AMMUNITION_TYPE, list[0] as String)] =
          (list[1] as ArrayList<*>).map { CddaItemRef(CddaType.ITEM, it as String) }.toMutableList()
      } else throw Exception("${any::class} is not JsonArray")
    }
  }
}
