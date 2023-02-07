package `fun`.hydd.cdda_browser.model.base

import com.googlecode.jmapper.annotations.JGlobalMap
import com.googlecode.jmapper.annotations.JMap
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.dao.JsonEntityDao
import `fun`.hydd.cdda_browser.entity.CddaItem
import `fun`.hydd.cdda_browser.entity.JsonEntity
import `fun`.hydd.cdda_browser.extension.getCollection
import `fun`.hydd.cdda_browser.extension.getHashString
import `fun`.hydd.cdda_browser.extension.getTranslation
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemData
import io.vertx.core.json.JsonObject
import org.hibernate.reactive.stage.Stage
import org.slf4j.LoggerFactory
import java.io.File

@JGlobalMap(classes = [CddaJsonParsedResult::class], excluded = ["id", "data", "log"])
class CddaItemParseDto() {
  @JMap(classes = [CddaItem::class])
  lateinit var jsonType: JsonType

  @JMap(classes = [CddaItem::class])
  lateinit var cddaType: CddaType

  lateinit var mod: CddaModParseDto

  // not is db primary key
  @JMap(value = "cddaId", classes = [CddaItem::class])
  lateinit var id: String

  lateinit var path: File

  lateinit var json: JsonObject

  var copyFrom: String? = null
  var abstract: Boolean = false
  var relative: JsonObject? = null
  var proportional: JsonObject? = null
  var extend: JsonObject? = null
  var delete: JsonObject? = null

  var data: CddaItemData? = null

  private val log = LoggerFactory.getLogger(this.javaClass)

  fun getValue(key: String, parentValue: Any?): Any? {
    return if (json.containsKey(key)) json.getValue(key)
    else parentValue
  }

  fun getValue(key: String, parentValue: Any?, def: Any): Any {
    return getValue(key, parentValue) ?: def
  }

  fun getString(key: String, parentValue: String?): String? {
    return if (json.containsKey(key)) json.getString(key)
    else parentValue
  }

  fun getString(key: String, parentValue: String?, def: String): String {
    return getString(key, parentValue) ?: def
  }

  fun getBoolean(key: String, parentValue: Boolean?): Boolean? {
    return if (json.containsKey(key)) json.getBoolean(key)
    else parentValue
  }

  fun getBoolean(key: String, parentValue: Boolean?, def: Boolean): Boolean {
    return getBoolean(key, parentValue) ?: def
  }

  fun getDouble(key: String, parentValue: Double?): Double? {
    val result = if (json.containsKey(key)) json.getDouble(key) as Double
    else parentValue
    return if (result != null) processDoubleProportionalAndRelative(result, key)
    else null
  }

  fun getDouble(key: String, parentValue: Double?, def: Double): Double {
    val result = if (json.containsKey(key)) json.getDouble(key) as Double
    else parentValue ?: def
    return processDoubleProportionalAndRelative(result, key)
  }

  fun getTranslation(key: String, ctxt: String? = null, parentValue: Translation?): Translation? {
    return if (json.containsKey(key)) json.getTranslation(key, ctxt)
    else parentValue
  }

  inline fun <reified T : Any> getCollection(key: String, parentValue: Collection<T>?): Collection<T>? {
    var result = if (json.containsKey(key)) json.getCollection<T>(key)?.toMutableList()
    else (parentValue)?.toMutableList()
    if (extend != null) {
      val extendValue = extend!!.getCollection<T>(key)
      if (extendValue != null) {
        if (result != null) result.addAll(extendValue)
        else result = extendValue.toMutableList()
      }
    }
    if (result != null) {
      if (delete != null) {
        val deleteValue = delete!!.getCollection<T>(key)
        if (deleteValue != null) result.removeAll(deleteValue)
      }
    }
    return result
  }

  inline fun <reified T : Any> getCollection(
    key: String,
    parentValue: Collection<T>?,
    def: Collection<T>
  ): Collection<T> {
    val result = if (json.containsKey(key)) json.getCollection(key, def).toMutableList()
    else (parentValue ?: def).toMutableList()
    if (extend != null) {
      val extendValue = extend!!.getCollection<T>(key)
      if (extendValue != null) {
        result.addAll(extendValue)
      }
    }
    if (delete != null) {
      val deleteValue = delete!!.getCollection<T>(key)
      if (deleteValue != null) result.removeAll(deleteValue)
    }
    return result
  }

  private fun processDoubleProportionalAndRelative(value: Double, key: String): Double {
    var result = value
    if (proportional != null) {
      val proportionalValue = proportional!!.getDouble(key, 1.0)
      if (proportionalValue != null) {
        result = processProportion(result, proportionalValue, key)
      }
    }
    if (relative != null) {
      val relativeValue = relative!!.getDouble(key, 0.0)
      if (relativeValue != null) {
        result += relativeValue
      }
    }
    return result
  }

  private fun processProportion(oldValue: Double, newValue: Double, key: String): Double {
    return if (validateProportionalValue(newValue, key)) oldValue * newValue else oldValue
  }

  private fun validateProportionalValue(proportionalValue: Double, key: String): Boolean {
    if (proportionalValue < 0 || proportionalValue > 1) {
      log.warn("${mod.modId}:$path's $json : $proportional's $key value $proportionalValue not in 0-1!")
      return false
    }
    return true
  }

  suspend fun toEntity(factory: Stage.SessionFactory): CddaItem {
    val cddaItem = CddaItem()
    cddaItem.cddaType = this.cddaType
    cddaItem.jsonType = this.jsonType
    cddaItem.cddaId = this.id
    cddaItem.path = this.path.absolutePath// todo change to relative path

    val originalJsonHash = this.json.getHashString()
    var originalJsonEntity = JsonEntityDao.findByHashCode(factory, originalJsonHash)
    if (originalJsonEntity == null) {
      originalJsonEntity = JsonEntity()
      originalJsonEntity.json = this.json
      originalJsonEntity.hashCode = originalJsonHash
    }
    cddaItem.originalJson = originalJsonEntity

    val json = JsonObject.mapFrom(this.data!!)
    val jsonHash = json.getHashString()
    var jsonEntity = JsonEntityDao.findByHashCode(factory, jsonHash)
    if (jsonEntity == null) {
      jsonEntity = JsonEntity()
      jsonEntity.json = json
      jsonEntity.hashCode = jsonHash
    }
    cddaItem.json = jsonEntity
    return cddaItem
  }
}
