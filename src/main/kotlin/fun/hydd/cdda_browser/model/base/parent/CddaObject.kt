package `fun`.hydd.cdda_browser.model.base.parent

import com.fasterxml.jackson.annotation.JsonInclude
import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation

@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class CddaObject {
  @IgnoreMap
  open val itemVersion = 0

  @IgnoreMap
  var itemName: Translation? = null

  @IgnoreMap
  var itemDescription: Translation? = null
  open fun finalize(commonItem: CddaCommonItem, itemRef: CddaItemRef) {
    return
  }
}
