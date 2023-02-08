package `fun`.hydd.cdda_browser.model.entity

import io.vertx.core.json.JsonObject
import org.hibernate.Hibernate
import org.hibernate.annotations.Type
import javax.persistence.*

@Entity
@Table(name = "json_entity")
open class JsonEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null

  @Column(name = "hash_code", nullable = false, unique = true, length = 256)
  open var hashCode: String? = null

  @Type(type = "fun.hydd.cdda_browser.model.userType.Json")
  @Column(columnDefinition = "json", name = "json", nullable = false)
  open var json: JsonObject? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as JsonEntity

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()
}
