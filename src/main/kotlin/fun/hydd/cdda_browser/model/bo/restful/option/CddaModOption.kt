package `fun`.hydd.cdda_browser.model.bo.restful.option

import `fun`.hydd.cdda_browser.model.base.Translation

data class CddaModOption(
  val id: String,
  val name: Translation,
  val description: Translation,
  val obsolete: Boolean,
  val core: Boolean,
  val depModIds: Collection<String>,
  val allDepModIds: Collection<String>,
)
