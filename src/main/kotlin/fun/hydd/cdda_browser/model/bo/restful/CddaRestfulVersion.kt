package `fun`.hydd.cdda_browser.model.bo.restful

import `fun`.hydd.cdda_browser.constant.CddaVersionStatus
import java.time.LocalDateTime

data class CddaRestfulVersion(
  val id: Long,
  val releaseName: String,
  val tagName: String,
  val commitHash: String,
  val status: CddaVersionStatus,
  val experiment: Boolean,
  val tagDate: LocalDateTime,
  val mods: Collection<CddaRestfulMod>,
  val pos: Collection<String>
)
