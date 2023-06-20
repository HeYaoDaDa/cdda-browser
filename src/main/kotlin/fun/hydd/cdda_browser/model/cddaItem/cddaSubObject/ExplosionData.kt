package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import io.vertx.core.json.JsonObject

data class ExplosionData(
  val power: Double = -1.0,
  val distanceFactor: Double = 0.75,
  val maxNoise: Int = 90000000,
  var fire: Boolean = false,
  @IgnoreMap @MapInfo(spFun = "shrapnelFun") var shrapnel: ShrapnelData? = null
) : CddaSubObject() {
  fun shrapnelFun(jsonValue: Any) {
    if (jsonValue is JsonObject && jsonValue.containsKey("shrapnel")) {
      when (val shrapnelJson = jsonValue.getValue("shrapnel")) {
        is Int -> this.shrapnel = ShrapnelData(casingMass = shrapnelJson)
        is JsonObject -> {
          this.shrapnel = ShrapnelData()
          this.shrapnel!!.parse(shrapnelJson, "")
        }

        else -> throw Exception()
      }
    }
  }
}
