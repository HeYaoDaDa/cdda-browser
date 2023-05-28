package `fun`.hydd.cdda_browser.model.bo.parse

import `fun`.hydd.cdda_browser.constant.CddaVersionStatus
import java.time.LocalDateTime

class CddaParseVersion {
  var id: Long? = null

  lateinit var releaseName: String

  lateinit var tagName: String

  lateinit var commitHash: String

  lateinit var status: CddaVersionStatus

  var experiment: Boolean = true

  lateinit var tagDate: LocalDateTime

  val mods: MutableCollection<CddaParseMod> = mutableListOf()
}
