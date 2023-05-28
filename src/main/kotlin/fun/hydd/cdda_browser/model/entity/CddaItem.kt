package `fun`.hydd.cdda_browser.model.entity

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseItem
import `fun`.hydd.cdda_browser.model.dao.JsonEntityDao
import `fun`.hydd.cdda_browser.util.extension.getHashString
import io.vertx.core.json.JsonObject
import org.hibernate.Hibernate
import org.hibernate.reactive.stage.Stage
import javax.persistence.*

@Entity
@Table(name = "cdda_item")
open class CddaItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null

  @ManyToOne
  @JoinColumn(name = "mod_id")
  open var mod: CddaMod? = null

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

  @Column(name = "abstract", nullable = false)
  open var abstract: Boolean? = false

  @Embedded
  @AttributeOverrides(
    AttributeOverride(name = "value", column = Column(name = "name_value", length = 1000)),
    AttributeOverride(name = "ctxt", column = Column(name = "name_ctxt"))
  )
  open var name: TranslationEntity? = null

  @Embedded
  @AttributeOverrides(
    AttributeOverride(name = "value", column = Column(name = "description_value", length = 1000)),
    AttributeOverride(name = "ctxt", column = Column(name = "description_ctxt"))
  )
  open var description: TranslationEntity? = null

  @ManyToOne(
    fetch = FetchType.LAZY,
    cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH],
    optional = false
  )
  @JoinColumn(name = "original_json_id", nullable = false)
  open var originalJson: JsonEntity? = null


  @ManyToOne(
    fetch = FetchType.LAZY,
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

  companion object {
    suspend fun of(factory: Stage.SessionFactory, mod: CddaMod, cddaParseItem: CddaParseItem): CddaItem {
      val originalJsonHash = cddaParseItem.json.getHashString()
      var originalJsonEntity = JsonEntityDao.findByHashCode(factory, originalJsonHash)
      if (originalJsonEntity == null) {
        originalJsonEntity = JsonEntity()
        originalJsonEntity.json = cddaParseItem.json
        originalJsonEntity.hashCode = originalJsonHash
      }

      val json = JsonObject.mapFrom(cddaParseItem.data!!)
      val jsonHash = json.getHashString()
      var jsonEntity = JsonEntityDao.findByHashCode(factory, jsonHash)
      if (jsonEntity == null) {
        jsonEntity = JsonEntity()
        jsonEntity.json = json
        jsonEntity.hashCode = jsonHash
      }

      val cddaItem = CddaItem()
      cddaItem.mod = mod
      cddaItem.cddaType = cddaParseItem.cddaType
      cddaItem.jsonType = cddaParseItem.jsonType
      cddaItem.cddaId = cddaParseItem.id
      cddaItem.path = cddaParseItem.path.absolutePath// todo change to relative path
      cddaItem.abstract = cddaParseItem.abstract
      cddaItem.originalJson = originalJsonEntity
      cddaItem.json = jsonEntity
      cddaItem.name = TranslationEntity.of(cddaParseItem.name)
      if (cddaParseItem.description != null) {
        cddaItem.description = TranslationEntity.of(cddaParseItem.description!!)
      }
      return cddaItem
    }
  }
}
