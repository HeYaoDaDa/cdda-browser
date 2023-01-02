package `fun`.hydd.cdda_browser.entity

import org.hibernate.Hibernate
import javax.persistence.*

@Entity
@Table(name = "get_text_po")
open class GetTextPo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null

  @Column(name = "language", nullable = false)
  open var language: String? = null

  @ManyToOne(optional = false)
  @JoinColumn(name = "cdda_version_id", nullable = false)
  open var version: CddaVersion? = null

  @ManyToOne(cascade = [CascadeType.ALL], optional = false)
  @JoinColumn(name = "file_entity_id", nullable = false)
  open var fileEntity: FileEntity? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as GetTextPo

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()
}
