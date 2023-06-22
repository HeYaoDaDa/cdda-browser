package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation

class MartialArt : CddaObject() {
  lateinit var name:Translation
  lateinit var description:Translation
  var initiate:MutableList<Translation> = mutableListOf()
}
