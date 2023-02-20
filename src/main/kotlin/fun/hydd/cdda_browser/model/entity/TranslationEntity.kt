package `fun`.hydd.cdda_browser.model.entity

import `fun`.hydd.cdda_browser.model.base.Translation
import javax.persistence.Embeddable

@Embeddable
open class TranslationEntity {
  var value: String? = null

  var ctxt: String? = null

  fun toTranslation(): Translation {
    return Translation(this.value!!, this.ctxt)
  }
}
