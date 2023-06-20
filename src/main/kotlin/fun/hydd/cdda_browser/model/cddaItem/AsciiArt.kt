package `fun`.hydd.cdda_browser.model.cddaItem

import com.fasterxml.jackson.annotation.JsonIgnore
import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef

class AsciiArt : CddaObject() {
  lateinit var id: String

  @IgnoreMap
  lateinit var picture: String

  @JsonIgnore
  @MapInfo(key = "picture")
  var pictureJson: MutableList<String> = mutableListOf()
  override fun finalize(commonItem: CddaCommonItem, itemRef: CddaItemRef) {
    val stringBuilder = StringBuilder()
    this.pictureJson.forEachIndexed { index, s ->
      stringBuilder.append(s)
      if (index < this.pictureJson.size - 1) stringBuilder.append("\n")
    }
    this.picture = stringBuilder.toString()
  }
}
