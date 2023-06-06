package `fun`.hydd.cdda_browser.model.cddaItem

import `fun`.hydd.cdda_browser.annotation.CddaItem
import `fun`.hydd.cdda_browser.annotation.CddaProperty
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.model.FinalResult
import `fun`.hydd.cdda_browser.model.ModOrder
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.Translation
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemData
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemParser

class SubBodyPart : CddaItemData() {
  lateinit var id: String
  lateinit var name: Translation
  var secondary: Boolean = false
  var maxCoverage: Double = 0.0
  var side: Int = 0
  var nameMultiple: Translation? = null
  lateinit var parent: CddaItemRef
  var opposite: CddaItemRef? = null
  var locationsUnder: MutableList<CddaItemRef> = mutableListOf()

  class Parser : CddaItemParser() {
    override fun parse(jsonEntity: Any, dependencies: MutableMap<CddaItemRef, ModOrder>): FinalResult {
      if (jsonEntity is JsonEntity) {
        val finalItem = SubBodyPart()
        finalItem.id = jsonEntity.id
        finalItem.name = jsonEntity.name
        finalItem.secondary = jsonEntity.secondary
        finalItem.maxCoverage = jsonEntity.maxCoverage
        finalItem.nameMultiple = jsonEntity.nameMultiple
        finalItem.side = jsonEntity.side
        finalItem.parent = jsonEntity.parent
        finalItem.opposite = jsonEntity.opposite
        finalItem.locationsUnder = jsonEntity.locationsUnder
        if (finalItem.locationsUnder.isEmpty())
          finalItem.locationsUnder.add(CddaItemRef(CddaType.SUB_BODY_PART, finalItem.id))
        return FinalResult(finalItem, dependencies, null)
      } else {
        throw Exception("class not match, class is ${jsonEntity::class}")
      }
    }
  }

  @CddaItem
  class JsonEntity() {
    lateinit var id: String
    lateinit var name: Translation
    var secondary: Boolean = false
    var maxCoverage: Double = 0.0
    var side: Int = 0
    var nameMultiple: Translation? = null

    @CddaProperty(para = "BODY_PART")
    lateinit var parent: CddaItemRef

    @CddaProperty(para = "SUB_BODY_PART")
    var opposite: CddaItemRef? = null

    @CddaProperty(para = "SUB_BODY_PART")
    var locationsUnder: MutableList<CddaItemRef> = mutableListOf()
  }
}
