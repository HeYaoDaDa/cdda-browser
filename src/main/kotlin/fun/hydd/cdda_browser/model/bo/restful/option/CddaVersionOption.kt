package `fun`.hydd.cdda_browser.model.bo.restful.option

import `fun`.hydd.cdda_browser.constant.CddaVersionStatus
import `fun`.hydd.cdda_browser.model.entity.CddaVersion
import java.time.ZoneOffset

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
) {
  companion object {
    fun of(cddaVersion: CddaVersion): CddaVersionOption {
      return CddaVersionOption(
        cddaVersion.id!!.toString(),
        cddaVersion.releaseName!!,
        cddaVersion.tagName!!,
        cddaVersion.commitHash!!,
        cddaVersion.status!!,
        cddaVersion.experiment!!,
        cddaVersion.tagDate!!.toInstant(ZoneOffset.UTC).toEpochMilli(),
        cddaVersion.mods.sortedBy { it.id }.map(CddaModOption::of),
        cddaVersion.pos.map { it.language!! },
      )
    }
  }
}
