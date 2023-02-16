package `fun`.hydd.cdda_browser.model.bo.parse

import `fun`.hydd.cdda_browser.model.entity.CddaMod
import `fun`.hydd.cdda_browser.model.entity.CddaVersion
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.hibernate.reactive.stage.Stage
import java.io.File

/**
 * A DTO for the {@link fun.hydd.cdda_browser.entity.CddaMod} entity
 */
class CddaParseMod {
  lateinit var id: String

  lateinit var name: String

  lateinit var description: String

  var obsolete: Boolean = false

  var core: Boolean = false

  lateinit var depModIds: Collection<String>

  lateinit var path: Collection<File>

  lateinit var depMods: Set<CddaParseMod>

  lateinit var cddaParsedJsons: Set<CddaParsedJson>

  val cddaItems: MutableCollection<CddaParseItem> = mutableSetOf()

  val cddaItemSize: Int by lazy {
    cddaParsedJsons.size
  }

  val allDepMods: Set<CddaParseMod> by lazy {
    depMods.flatMap { it.allDepMods + it }.toHashSet()
  }

  val allDepModIds: Set<String> by lazy {
    allDepMods.map { it.id }.toHashSet()
  }

  suspend fun toCddaMod(factory: Stage.SessionFactory, version: CddaVersion): CddaMod {
    val cddaMod = CddaMod()
    cddaMod.modId = this.id
    cddaMod.name = this.name
    cddaMod.description = this.description
    cddaMod.obsolete = this.obsolete
    cddaMod.core = this.core
    cddaMod.depModIds.addAll(this.depModIds)
    cddaMod.allDepModIds.addAll(this.allDepModIds)
    val cddaItems = coroutineScope {
      cddaItems.map {
        async {
          val cddaItem = it.toCddaItem(factory, cddaMod)
          cddaItem
        }
      }.awaitAll()
    }
    cddaMod.items = cddaItems.toMutableSet()
    cddaMod.version = version
    return cddaMod
  }

}
