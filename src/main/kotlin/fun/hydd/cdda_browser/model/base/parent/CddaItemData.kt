package `fun`.hydd.cdda_browser.model.base.parent

import `fun`.hydd.cdda_browser.model.base.Translation

abstract class CddaItemData() {
  open val itemVersion = 0
  var itemName: Translation? = null
  var itemDescription: Translation? = null
}
