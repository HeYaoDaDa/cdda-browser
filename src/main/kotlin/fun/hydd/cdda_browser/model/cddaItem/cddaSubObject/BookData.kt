package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit.Time

data class BookData(
  var maxLevel: Int = 0,
  var requiredLevel: Int = 0,
  var `fun`: Int = 0,
  var intelligence: Int = 0,
  @MapInfo(param = "M")
  var time: Time = Time(),
  @MapInfo(param = "NULL")//todo change to Skill
  var skill: CddaItemRef? = null,
  @MapInfo(param = "NULL")//todo change to martial
  var martialArt: CddaItemRef? = null,
  var chapters: Int = 0,
  var proficiencies: MutableList<BookProficiencyBonus> = mutableListOf(),
  var scannable: Boolean = true,
) : CddaSubObject() {

  data class BookProficiencyBonus(
    @MapInfo(param = "NULL")//todo change to proficiency
    var proficiency: CddaItemRef = CddaItemRef(),
    var failFactor: Double = 0.5,
    var timeFactor: Double = 0.5,
    var includePrereqs: Boolean = true
  ) : CddaSubObject()
}
