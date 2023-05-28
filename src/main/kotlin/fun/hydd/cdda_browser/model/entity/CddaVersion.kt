package `fun`.hydd.cdda_browser.model.entity

import `fun`.hydd.cdda_browser.constant.CddaVersionStatus
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseVersion
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.hibernate.reactive.stage.Stage
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "cdda_version")
open class CddaVersion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null

  @Column(name = "release_name", nullable = false)
  open var releaseName: String? = null

  @Column(name = "tag_name", nullable = false)
  open var tagName: String? = null

  @Column(name = "commit_hash", nullable = false)
  open var commitHash: String? = null

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  open var status: CddaVersionStatus? = null

  @Column(name = "experiment", nullable = false)
  open var experiment: Boolean? = null

  @Column(name = "tag_date", nullable = false)
  open var tagDate: LocalDateTime? = null

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "version", cascade = [CascadeType.ALL], orphanRemoval = true)
  open var mods: MutableSet<CddaMod> = mutableSetOf()

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "version", cascade = [CascadeType.ALL], orphanRemoval = true)
  open var pos: MutableSet<GetTextPo> = mutableSetOf()

  companion object {
    suspend fun of(factory: Stage.SessionFactory, cddaParseVersion: CddaParseVersion): CddaVersion {
      val cddaVersion = CddaVersion()
      cddaVersion.releaseName = cddaParseVersion.releaseName
      cddaVersion.tagName = cddaParseVersion.releaseName
      cddaVersion.commitHash = cddaParseVersion.commitHash
      cddaVersion.status = cddaParseVersion.status
      cddaVersion.experiment = cddaParseVersion.experiment
      cddaVersion.tagDate = cddaParseVersion.tagDate
      cddaVersion.mods.addAll(coroutineScope {
        cddaParseVersion.mods.map {
          async {
            CddaMod.of(factory, cddaVersion, it)
          }
        }.awaitAll()
      })
      return cddaVersion
    }
  }
}
