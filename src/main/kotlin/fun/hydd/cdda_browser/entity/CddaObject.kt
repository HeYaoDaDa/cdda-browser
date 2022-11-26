package `fun`.hydd.cdda_browser.entity

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.converter.JsonObjectConverter
import io.vertx.core.json.JsonObject
import org.hibernate.Hibernate
import javax.persistence.*

@Entity
@Table(name = "cdda_object")
open class CddaObject {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null

  @Enumerated(EnumType.STRING)
  @Column(name = "json_type", nullable = false)
  open var jsonType: JsonType? = null

  @Enumerated
  @Column(name = "cdda_type", nullable = false)
  open var cddaType: CddaType? = null

  @Lob
  @Convert(converter = JsonObjectConverter::class)
  @Column(name = "json_object", nullable = false)
  open var jsonObject: JsonObject? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as CddaObject

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()
}
