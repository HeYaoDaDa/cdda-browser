package `fun`.hydd.cdda_browser.model.entity

import `fun`.hydd.cdda_browser.constant.CddaVersionStatus
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

  @Column(name = "release_date", nullable = false)
  open var releaseDate: LocalDateTime? = null

  @Column(name = "tag_date", nullable = false)
  open var tagDate: LocalDateTime? = null

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "cddaVersion", cascade = [CascadeType.ALL], orphanRemoval = true)
  open var cddaMods: MutableSet<CddaMod> = mutableSetOf()

  @OneToMany(mappedBy = "version", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
  open var pos: MutableSet<GetTextPo> = mutableSetOf()
}
