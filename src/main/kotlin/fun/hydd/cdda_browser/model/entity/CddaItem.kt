package `fun`.hydd.cdda_browser.model.entity

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.model.FinalCddaItem
import `fun`.hydd.cdda_browser.model.dao.JsonEntityDao
import `fun`.hydd.cdda_browser.util.extension.getHashString
import io.vertx.core.json.JsonObject
import org.hibernate.Hibernate
import org.hibernate.reactive.stage.Stage
import java.nio.file.Path
import javax.persistence.*
import kotlin.io.path.relativeTo

@Entity
@Table(name = "cdda_item")
open class CddaItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  open var id: Long? = null

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

  @ManyToOne
  @JoinColumn(name = "cdda_mod_id")
  open var cddaMod: CddaMod? = null

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as CddaItem

    return id != null && id == other.id
  }

  override fun hashCode(): Int = javaClass.hashCode()

  companion object {
    suspend fun ofList(
      factory: Stage.SessionFactory,
      repoPath: Path,
      finalCddaItems: List<FinalCddaItem>,
      mods: List<CddaMod>
    ): List<CddaItem> {
      val currentJsonEntityMap = mutableMapOf<String, JsonEntity>()
      return finalCddaItems.map { of(factory, repoPath, currentJsonEntityMap, it, mods) }
    }

    suspend fun of(
      factory: Stage.SessionFactory,
      repoPath: Path,
      currentJsonEntityMap: MutableMap<String, JsonEntity>,
      finalCddaItem: FinalCddaItem,
      mods: List<CddaMod>
    ): CddaItem {
      val originalJsonHash = finalCddaItem.originalJson.getHashString()
      var originalJsonEntity =
        JsonEntityDao.findByHashCode(factory, originalJsonHash) ?: currentJsonEntityMap[originalJsonHash]
      if (originalJsonEntity == null) {
        originalJsonEntity = JsonEntity()
        originalJsonEntity.json = finalCddaItem.originalJson
        originalJsonEntity.hashCode = originalJsonHash
        currentJsonEntityMap[originalJsonHash] = originalJsonEntity
      }

      val json = JsonObject.mapFrom(finalCddaItem.cddaObject)
      val jsonHash = json.getHashString()
      var jsonEntity = JsonEntityDao.findByHashCode(factory, jsonHash) ?: currentJsonEntityMap[jsonHash]
      if (jsonEntity == null) {
        jsonEntity = JsonEntity()
        jsonEntity.json = json
        jsonEntity.hashCode = jsonHash
        currentJsonEntityMap[jsonHash] = jsonEntity
      }

      val cddaItem = CddaItem()
      cddaItem.cddaMod = mods.find { it.modId == finalCddaItem.getMod().id }
      cddaItem.cddaMod!!.cddaItems.add(cddaItem)
      cddaItem.cddaType = finalCddaItem.cddaType
      cddaItem.jsonType = finalCddaItem.jsonType
      cddaItem.cddaId = finalCddaItem.id
      cddaItem.path = finalCddaItem.path.relativeTo(repoPath).toString()
      cddaItem.abstract = finalCddaItem.abstract
      cddaItem.originalJson = originalJsonEntity
      cddaItem.json = jsonEntity
      if (finalCddaItem.name != null)
        cddaItem.name = TranslationEntity.of(finalCddaItem.name)
      if (finalCddaItem.description != null)
        cddaItem.description = TranslationEntity.of(finalCddaItem.description)
      return cddaItem
    }
  }
}
