package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.Translation

@Suppress("unused")
class Skill : CddaObject() {
  lateinit var name: Translation
  lateinit var description: Translation
  var tags: MutableList<String> = mutableListOf()

  @MapInfo(param = "SKILL_DISPLAY_TYPE")
  lateinit var displayCategory: CddaItemRef
  var timeToAttack: TimeToAttack? = null
  var companionCombatRankFactor: Int = 0
  var companionSurvivalRankFactor: Int = 0
  var companionIndustryRankFactor: Int = 0
  var obsolete: Boolean = false

  data class TimeToAttack(
    var minTime: Int = 50,
    var baseTime: Int = 220,
    var timeReductionPerLevel: Int = 25,
  ) : CddaSubObject()
}
