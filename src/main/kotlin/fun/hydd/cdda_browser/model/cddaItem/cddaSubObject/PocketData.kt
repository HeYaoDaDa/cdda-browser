package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Length
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Volume
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Weight
import io.vertx.core.json.JsonObject
import kotlin.math.cbrt

data class PocketData(
  var pocketType: String = "CONTAINER",
  @IgnoreMap
  @MapInfo(spFun = "ammoRestrictionFun")
  var ammoRestriction: MutableMap<String, Int> = mutableMapOf(),
  var description: Translation? = null,
  var name: Translation? = null,
  var minItemVolume: Volume = Volume(0),
  var maxItemVolume: Volume? = null,
  var maxContainsVolume: Volume = Volume(200000000),
  var maxContainsWeight: Weight = Weight(2000000 * 1000),
  @MapInfo(spFun = "maxItemLengthFun")
  var maxItemLength: Length = Length(-1),
  var extraEncumbrance: Int = 0,
  var volumeEncumberModifier: Double = 1.0,
  var ripoff: Int = 0,
  var spoilMultiplier: Double = 1.0,
  var weightMultiplier: Double = 1.0,
  var volumeMultiplier: Double = 1.0,
  var magazineWell: Volume = Volume(0),
  var moves: Int = 100,
  var fireProtection: Boolean = false,
  var watertight: Boolean = false,
  var airtight: Boolean = false,
  var openContainer: Boolean = false,
  var transparent: Boolean = false,
  var rigid: Boolean = false,
  var forbidden: Boolean = false,
  var holster: Boolean = false,
  var ablative: Boolean = false,
  var inheritsFlags: Boolean = false,
  var sealedData: SealableData = SealableData(),
  @MapInfo(param = "JSON_FLAG")
  var flagRestriction: MutableList<CddaItemRef> = mutableListOf(),
  @MapInfo(param = "ITEM")
  var itemRestriction: MutableList<CddaItemRef> = mutableListOf(),
  @MapInfo(param = "MATERIAL")
  var materialRestriction: MutableList<CddaItemRef> = mutableListOf(),
  @MapInfo(param = "ITEM")
  var allowedSpeedloaders: MutableList<CddaItemRef> = mutableListOf(),
  @MapInfo(param = "ITEM")
  var defaultMagazine: CddaItemRef? = null,
) : CddaSubObject() {

  fun maxItemLengthFun() {
    if (maxItemLength.value == -1L) {
      maxItemLength.value = ((cbrt(this.maxContainsVolume.value.toDouble())).toInt() * 1.4142135623730951).toLong()
    }
  }

  fun ammoRestrictionFun(jsonObject: JsonObject) {
    jsonObject.getJsonObject("ammo_restriction")?.forEach {
      this.ammoRestriction[it.key] = it.value as Int
    }
  }

  override fun finalize(jsonValue: Any, param: String) {
    if (this.itemRestriction.isNotEmpty())
      this.defaultMagazine = this.itemRestriction.first()
    if (this.ablative) this.holster = true
  }

  data class SealableData(var spoilMultiplier: Double = 1.0) : CddaSubObject()
}
