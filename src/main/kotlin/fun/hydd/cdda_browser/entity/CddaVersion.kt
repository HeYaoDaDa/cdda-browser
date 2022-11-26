package `fun`.hydd.cdda_browser.entity

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

  @Column(name = "name", nullable = false)
  open var name: String? = null

  @Column(name = "tag_name", nullable = false)
  open var tagName: String? = null

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  open var status: CddaVersionStatus? = null

  @Column(name = "experiment", nullable = false)
  open var experiment: Boolean? = null

  @Column(name = "published_at", nullable = false)
  open var publishedAt: LocalDateTime? = null

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "cddaVersion", cascade = [CascadeType.ALL], orphanRemoval = true)
  open var cddaMods: MutableSet<CddaMod> = mutableSetOf()
}
