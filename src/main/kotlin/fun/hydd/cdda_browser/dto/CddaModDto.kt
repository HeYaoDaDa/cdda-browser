package `fun`.hydd.cdda_browser.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import `fun`.hydd.cdda_browser.entity.CddaMod
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.hibernate.reactive.stage.Stage
import java.io.File

/**
 * A DTO for the {@link fun.hydd.cdda_browser.entity.CddaMod} entity
 */
data class CddaModDto(
  val modId: String,
  val name: String,
  val description: String,
  val obsolete: Boolean = false,
  val core: Boolean = false,
  val depModIds: MutableSet<String> = mutableSetOf(),
  val path: Set<File>,
) {
  lateinit var depMods: Set<CddaModDto>

  @JsonIgnore
  lateinit var cddaJsons: Set<CddaJson>

  val cddaItemSize: Int by lazy {
    cddaJsons.size
  }

  val allDepMods: Set<CddaModDto> by lazy {
    depMods.flatMap { it.allDepMods + it }.toHashSet()
  }

  val allDepModIds: Set<String> by lazy {
    allDepMods.map { it.modId }.toHashSet()
  }

  suspend fun toEntity(factory: Stage.SessionFactory, cddaItems: Collection<CddaItem>): CddaMod {
    val result = CddaMod()
    result.modId = this.modId
    result.name = this.name
    result.description = this.description
    result.obsolete = this.obsolete
    result.core = this.core
    result.depModIds.clear()
    result.depModIds.addAll(this.depModIds)
    val cddaObjects = coroutineScope { cddaItems.map { async { it.toEntity(factory, result) } }.awaitAll() }
    result.cddaObjects = cddaObjects.toMutableSet()
    return result
  }
}
