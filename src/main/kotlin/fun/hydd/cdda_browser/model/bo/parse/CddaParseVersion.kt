package `fun`.hydd.cdda_browser.model.bo.parse

import `fun`.hydd.cdda_browser.constant.CddaVersionStatus
import `fun`.hydd.cdda_browser.model.entity.CddaVersion
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.hibernate.reactive.stage.Stage
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

  suspend fun toCddaVersion(factory: Stage.SessionFactory): CddaVersion {
    val cddaVersion = CddaVersion()
    cddaVersion.releaseName = this.releaseName
    cddaVersion.tagName = this.releaseName
    cddaVersion.commitHash = this.commitHash
    cddaVersion.status = this.status
    cddaVersion.experiment = this.experiment
    cddaVersion.tagDate = this.tagDate
    cddaVersion.mods.addAll(coroutineScope {
      mods.map {
        async {
          val cddaMod = it.toCddaMod(factory, cddaVersion)
          cddaMod
        }
      }.awaitAll()
    })
    return cddaVersion
  }
}
