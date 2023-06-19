package `fun`.hydd.cdda_browser.model.bo.restful.option

import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation
import `fun`.hydd.cdda_browser.model.entity.CddaMod

data class CddaModOption(
    val id: String,
    val name: Translation,
    val description: Translation,
    val obsolete: Boolean,
    val core: Boolean,
    val depModIds: Collection<String>,
    val allDepModIds: Collection<String>,
) {
  companion object {
    fun of(cddaMod: CddaMod): CddaModOption {
      return CddaModOption(
        cddaMod.modId!!,
        Translation(cddaMod.name!!),
        Translation(cddaMod.description!!),
        cddaMod.obsolete!!,
        cddaMod.core!!,
        cddaMod.depModIds,
        cddaMod.allDepModIds
      )
    }
  }
}
