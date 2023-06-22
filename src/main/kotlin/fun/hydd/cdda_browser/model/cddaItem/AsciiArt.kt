package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject

@Suppress("unused")
class AsciiArt : CddaObject() {
  @MapInfo(key = "picture")
  lateinit var picture: MutableList<String>
}
