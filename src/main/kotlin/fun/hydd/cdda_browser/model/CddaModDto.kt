package `fun`.hydd.cdda_browser.model

import java.nio.file.Path

data class CddaModDto(
  val id: String,
  val name: String,
  val description: String,
  val category: String,
  val dependencies: Set<CddaModDto>,
  val allDependencies: Set<CddaModDto>,
  val authors: Set<String>,
  val maintainers: Set<String>,
  val version: String?,
  val path: Set<Path>,
  val core: Boolean,
  val obsolete: Boolean,
) {
  companion object {
    fun ofList(entities: List<CddaModJsonEntity>): List<CddaModDto> {
      val entityMap = entities.associateBy { it.id }
      val cddaModMap = mutableMapOf<String, CddaModDto>()
      val result = mutableListOf<CddaModDto>()
      for (entity in entities) {
        val cddaMod = of(entity, entityMap, cddaModMap)
        cddaModMap[cddaMod.id] = cddaMod
        result.add(cddaMod)
      }
      return result
    }

    fun of(
      entity: CddaModJsonEntity,
      entityMap: Map<String, CddaModJsonEntity>,
      cddaModMap: Map<String, CddaModDto>
    ): CddaModDto {
      val dependencies = entity.dependencies.mapNotNull { cddaModMap[it] }.toSet()
      val allDependencies = entity.getAllDependencies(entityMap).mapNotNull { cddaModMap[it] }.toSet()
      val modPaths = mutableSetOf(entity.realPath)
      if (entity.path != null) {
        modPaths.add(entity.realPath.resolve(entity.path).toRealPath())
      }
      return CddaModDto(
        entity.id,
        entity.name,
        entity.description,
        convertCategory(entity.category),
        dependencies,
        allDependencies,
        entity.authors,
        entity.maintainers,
        entity.version,
        modPaths,
        entity.core,
        entity.obsolete
      )
    }

    private fun convertCategory(value: String?): String {
      return when (value) {
        "total_conversion" -> "TOTAL CONVERSIONS"
        "content" -> "CORE CONTENT PACKS"
        "items" -> "ITEM ADDITION MODS"
        "creatures" -> "CREATURE MODS"
        "misc_additions" -> "MISC ADDITIONS"
        "buildings" -> "BUILDINGS MODS"
        "vehicles" -> "VEHICLE MODS"
        "rebalance" -> "REBALANCING MODS"
        "magical" -> "MAGICAL MODS"
        "item_exclude" -> "ITEM EXCLUSION MODS"
        "monster_exclude" -> "MONSTER EXCLUSION MODS"
        "graphical" -> "GRAPHICAL MODS"
        else -> "NO CATEGORY"
      }
    }
  }
}
