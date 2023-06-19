package `fun`.hydd.cdda_browser.model.entity

import `fun`.hydd.cdda_browser.model.CddaModDto
import org.hibernate.Hibernate
import javax.persistence.*

@Entity
@Table(name = "cdda_mod")
open class CddaMod {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null

  @Column(name = "mod_id", nullable = false)
  open var modId: String? = null

  @Column(name = "name", nullable = false)
  open var name: String? = null

  @Column(name = "description", nullable = false, length = 1000)
  open var description: String? = null

  @ManyToOne(
    cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH],
    optional = false
  )
  @JoinColumn(name = "version_id", nullable = false)
  open var version: CddaVersion? = null

  @Column(name = "obsolete", nullable = false)
  open var obsolete: Boolean? = null

  @Column(name = "core", nullable = false)
  open var core: Boolean? = null

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "cdda_mod_depModIds", joinColumns = [JoinColumn(name = "owner_id")])
  @Column(name = "dep_mod_id")
  open var depModIds: MutableSet<String> = mutableSetOf()

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "cdda_mod_allDepModIds", joinColumns = [JoinColumn(name = "owner_id")])
  @Column(name = "all_dep_mod_id")
  open var allDepModIds: MutableSet<String> = mutableSetOf()

  @OneToMany(mappedBy = "cddaMod", cascade = [CascadeType.ALL], orphanRemoval = true)
  open var cddaItems: MutableSet<CddaItem> = mutableSetOf()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as CddaMod

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  companion object {
    fun of(cddaModDto: CddaModDto): CddaMod {
      val cddaMod = CddaMod()
      cddaMod.modId = cddaModDto.id
      cddaMod.name = cddaModDto.name
      cddaMod.description = cddaModDto.description
      cddaMod.obsolete = cddaModDto.obsolete
      cddaMod.core = cddaModDto.core
      cddaMod.depModIds.addAll(cddaModDto.dependencies.map { it.id })
      cddaMod.allDepModIds.addAll(cddaModDto.allDependencies.map { it.id })
      return cddaMod
    }
  }
}
