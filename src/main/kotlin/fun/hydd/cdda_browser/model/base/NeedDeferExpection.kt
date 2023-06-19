package `fun`.hydd.cdda_browser.model.base

import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef

class NeedDeferException(val defer: CddaItemRef) : Exception()
