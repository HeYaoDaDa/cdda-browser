package `fun`.hydd.cdda_browser.model.base.parent

import com.fasterxml.jackson.annotation.JsonInclude
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation

@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class CddaObject {
  @MapInfo(ignore = true)
  open val itemVersion = 0

  @MapInfo(ignore = true)
  var itemName: Translation? = null

  @MapInfo(ignore = true)
  var itemDescription: Translation? = null
  open fun finalize(commonItem: CddaCommonItem, itemRef: CddaItemRef) {
    return
  }
}
