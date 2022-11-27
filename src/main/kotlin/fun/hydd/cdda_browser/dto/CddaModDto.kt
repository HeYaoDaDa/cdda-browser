package `fun`.hydd.cdda_browser.dto

import `fun`.hydd.cdda_browser.entity.CddaMod

/**
 * A DTO for the {@link fun.hydd.cdda_browser.entity.CddaMod} entity
 */
data class CddaModDto(
  var id: Long? = null,
  var modId: String? = null,
  var name: String? = null,
  var description: String? = null,
  var obsolete: Boolean = false,
  var core: Boolean = false,
  var depModIds: MutableSet<String> = mutableSetOf()
// TODO
) {
  fun toEntity(): CddaMod {
    val result = CddaMod()
    result.id = this.id
    result.name = this.name
    result.description = this.description
    result.obsolete = this.obsolete
    result.core = this.core
    result.depModIds.clear()
    result.depModIds.addAll(this.depModIds)
    return result
  }
}
