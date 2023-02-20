package `fun`.hydd.cdda_browser.model.entity

import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.model.bo.restful.data.CddaModData
import `fun`.hydd.cdda_browser.model.bo.restful.option.CddaModOption
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

  @OneToMany(mappedBy = "mod", cascade = [CascadeType.ALL], orphanRemoval = true)
  open var items: MutableSet<CddaItem> = mutableSetOf()

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "cdda_mod_depModIds", joinColumns = [JoinColumn(name = "owner_id")])
  @Column(name = "dep_mod_id")
  open var depModIds: MutableSet<String> = mutableSetOf()

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "cdda_mod_allDepModIds", joinColumns = [JoinColumn(name = "owner_id")])
  @Column(name = "all_dep_mod_id")
  open var allDepModIds: MutableSet<String> = mutableSetOf()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as CddaMod

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  fun toCddaRestfulMod(): CddaModOption {
    return CddaModOption(
      this.modId!!,
      Translation(this.name!!),
      Translation(this.description!!),
      this.obsolete!!,
      this.core!!,
      this.depModIds,
      this.allDepModIds
    )
  }

  fun toCddaWithItemRestfulMod(): CddaModData {
    return CddaModData(
      this.modId!!,
      this.items.map { it.toCddaRestfulItem() }
    )
  }
}
