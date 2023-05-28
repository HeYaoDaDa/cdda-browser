package `fun`.hydd.cdda_browser.model.bo.restful.data

import `fun`.hydd.cdda_browser.model.entity.CddaMod

data class CddaModData(
  val id: String,
  val items: Collection<CddaItemData>
) {
  companion object {
    fun of(cddaMod: CddaMod): CddaModData {
      return CddaModData(
        cddaMod.modId!!,
        cddaMod.items.map(CddaItemData::of)
      )
    }
  }
}
