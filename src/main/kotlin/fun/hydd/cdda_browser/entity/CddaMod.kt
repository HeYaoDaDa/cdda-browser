package `fun`.hydd.cdda_browser.entity

import org.hibernate.Hibernate
import javax.persistence.*

@Entity
@Table(name = "cdda_mod")
open class CddaMod {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null

  @Column(name = "name", nullable = false)
  open var name: String? = null

  @ManyToOne(
    cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH],
    optional = false
  )
  @JoinColumn(name = "cdda_version_id", nullable = false)
  open var cddaVersion: CddaVersion? = null

  @Column(name = "obsolete", nullable = false)
  open var obsolete: Boolean? = null

  @Column(name = "core", nullable = false)
  open var core: Boolean? = null

  @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
  @JoinTable(
    name = "cdda_mod_cddaObjects",
    joinColumns = [JoinColumn(name = "cddaMod_id")],
    inverseJoinColumns = [JoinColumn(name = "cddaObjects_id")]
  )
  open var cddaObjects: MutableSet<CddaObject> = mutableSetOf()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as CddaMod

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()
}
