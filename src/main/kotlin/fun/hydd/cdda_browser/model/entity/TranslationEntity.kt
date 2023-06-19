package `fun`.hydd.cdda_browser.model.entity

import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation
import javax.persistence.Embeddable

@Embeddable
open class TranslationEntity {
  var value: String? = null

  var ctxt: String? = null

  companion object {
    fun of(translation: Translation): TranslationEntity {
      val result = TranslationEntity()
      result.value = translation.value
      result.ctxt = translation.ctxt
      return result
    }
  }
}
