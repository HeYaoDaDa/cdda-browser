package `fun`.hydd.cdda_browser.entity

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import org.hibernate.Hibernate
import javax.persistence.*

@Entity
@Table(name = "cdda_item")
open class CddaItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null

  @ManyToOne
  @JoinColumn(name = "cdda_mod_id")
  open var cddaMod: CddaMod? = null

  @Enumerated(EnumType.STRING)
  @Column(name = "json_type", nullable = false)
  open var jsonType: JsonType? = null

  @Enumerated(EnumType.STRING)
  @Column(name = "cdda_type", nullable = false)
  open var cddaType: CddaType? = null

  @Column(name = "cdda_id", nullable = false)
  open var cddaId: String? = null

  @Column(name = "path", nullable = false)
  open var path: String? = null

  @ManyToOne(
    cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH],
    optional = false
  )
  @JoinColumn(name = "original_json_id", nullable = false)
  open var originalJson: JsonEntity? = null


  @ManyToOne(
    cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH],
    optional = false
  )
  @JoinColumn(name = "json_id", nullable = false)
  open var json: JsonEntity? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as CddaItem

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()
}
