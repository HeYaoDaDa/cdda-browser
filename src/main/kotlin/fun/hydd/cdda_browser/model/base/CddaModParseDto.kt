package `fun`.hydd.cdda_browser.model.base

import com.fasterxml.jackson.annotation.JsonIgnore
import com.googlecode.jmapper.JMapper
import com.googlecode.jmapper.annotations.JMap
import com.googlecode.jmapper.enums.ChooseConfig
import `fun`.hydd.cdda_browser.entity.CddaMod
import `fun`.hydd.cdda_browser.extension.getCollection
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.hibernate.reactive.stage.Stage
import java.io.File

/**
 * A DTO for the {@link fun.hydd.cdda_browser.entity.CddaMod} entity
 */
class CddaModParseDto() {
  @JMap
  lateinit var modId: String

  @JMap
  lateinit var name: String

  @JMap
  lateinit var description: String

  @JMap
  var obsolete: Boolean = false

  @JMap
  var core: Boolean = false

  lateinit var depModIds: Collection<String>

  lateinit var path: Collection<File>

  lateinit var depMods: Set<CddaModParseDto>

  @JsonIgnore
  lateinit var cddaJsonParseDtos: Set<CddaJsonParseDto>

  @JsonIgnore
  val cddaItems: MutableCollection<CddaItemParseDto> = mutableSetOf()

  val cddaItemSize: Int by lazy {
    cddaJsonParseDtos.size
  }

  val allDepMods: Set<CddaModParseDto> by lazy {
    depMods.flatMap { it.allDepMods + it }.toHashSet()
  }

  val allDepModIds: Set<String> by lazy {
    allDepMods.map { it.modId }.toHashSet()
  }

  suspend fun toEntity(factory: Stage.SessionFactory): CddaMod {
    val jMapper = JMapper(CddaMod::class.java, CddaModParseDto::class.java, ChooseConfig.SOURCE)
    val cddaMod = jMapper.getDestination(this)
    cddaMod.depModIds.addAll(this.depModIds)
    val cddaItems = coroutineScope {
      cddaItems.map {
        async {
          val cddaItem = it.toEntity(factory)
          cddaItem.cddaMod = cddaMod
          cddaItem
        }
      }.awaitAll()
    }
    cddaMod.cddaItems = cddaItems.toMutableSet()
    return cddaMod
  }

  companion object {
    fun of(jsonObject: JsonObject, paths: Collection<File>): CddaModParseDto {
      val cddaModDto = CddaModParseDto()
      cddaModDto.modId = jsonObject.getString("id")
      cddaModDto.name = jsonObject.getString("name")
      cddaModDto.description = jsonObject.getString("description")
      cddaModDto.obsolete = jsonObject.getBoolean("obsolete", false)
      cddaModDto.core = jsonObject.getBoolean("core", false)
      cddaModDto.depModIds = jsonObject.getCollection<String>("dependencies", listOf()).toHashSet()
      cddaModDto.path = paths
      return cddaModDto
    }
  }
}
