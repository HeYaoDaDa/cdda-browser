package `fun`.hydd.cdda_browser.model.bo.restful.option

import `fun`.hydd.cdda_browser.constant.CddaVersionStatus

data class CddaVersionOption(
  val id: String,
  val releaseName: String,
  val tagName: String,
  val commitHash: String,
  val status: CddaVersionStatus,
  val experiment: Boolean,
  val tagDate: Long,
  val mods: Collection<CddaModOption>,
  val pos: Collection<String>
)
