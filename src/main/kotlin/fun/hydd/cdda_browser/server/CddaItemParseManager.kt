package `fun`.hydd.cdda_browser.server

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemParser
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseItem
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseMod
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseVersion
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParsedJson
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CddaItemParseManager {
  private val log = LoggerFactory.getLogger(this.javaClass)

  fun parseCddaVersion(cddaParseVersion: CddaParseVersion) {
    val pendQueue = PendQueue(cddaParseVersion.mods)
    val finalQueue = FinalQueue()
    cddaParseVersion.mods.forEach { mod ->
      val deferQueue = DeferQueue()
      for (cddaType in CddaType.values()) {
        val cddaItems = pendQueue.pop(mod, cddaType).flatMap { parseId(it) }
        mod.cddaItems.addAll(cddaItems)
        parseCddaItems(finalQueue, deferQueue, mod, cddaItems)
      }
      // print defer status
      val deferCddaItem = deferQueue.typeIdItemsMap.values.map { it.values.flatten() }.flatten()
      log.info(
        "mod ${mod.id} defer item size is ${deferCddaItem.size}, final size is ${finalQueue.getModItemsSize(mod)}"
      )
      if (deferCddaItem.isNotEmpty()) {
        deferCddaItem.forEach {
          log.warn("${it.cddaType} : ${it.id} is defer")
        }
        throw Exception("Has defer Cdda Item")
      }
    }
  }

  private fun parseCddaItems(
    finalQueue: FinalQueue,
    deferQueue: DeferQueue,
    mod: CddaParseMod,
    cddaParseItems: Collection<CddaParseItem>
  ) {
    for (cddaItem in cddaParseItems) {
      val parser = cddaItem.cddaType.parser
      val parentItemId = cddaItem.copyFrom
      if (parentItemId != null) {
        // have copy-from field
        val parentItem = finalQueue.find(mod, cddaItem.cddaType, parentItemId).firstOrNull()
        if (parentItem != null) {
          parseCddaItemWithoutParent(finalQueue, deferQueue, mod, cddaItem, parser, parentItem)
        } else {
          deferQueue.add(cddaItem, cddaItem.cddaType, parentItemId)
        }
      } else {
        parseCddaItemWithoutParent(finalQueue, deferQueue, mod, cddaItem, parser, null)
      }
    }
  }

  private fun parseCddaItemWithoutParent(
    finalQueue: FinalQueue,
    deferQueue: DeferQueue,
    mod: CddaParseMod,
    cddaItem: CddaParseItem,
    parser: CddaItemParser,
    parentItem: CddaParseItem?
  ) {
    val ref = parser.parse(cddaItem, parentItem?.data)
    if (ref !== null) deferQueue.add(cddaItem, ref.type, ref.id)
    else {
      cddaItem.name = parser.getName(cddaItem, cddaItem.data!!)
      finalQueue.add(cddaItem)
      val deferItems = deferQueue.pop(cddaItem.cddaType, cddaItem.id)
      if (deferItems.isNotEmpty()) parseCddaItems(finalQueue, deferQueue, mod, deferItems)
    }
  }

  private fun parseId(cddaParsedJson: CddaParsedJson): List<CddaParseItem> {
    val parser = cddaParsedJson.cddaType.parser
    val ids = parser.parseIds(cddaParsedJson)
    if (ids.isEmpty()) throw Exception("Parse id is empty")
    return ids
      .map {
        val cddaItemDto = cddaParsedJson.toCddaParseItem()
        cddaItemDto.id = it
        cddaItemDto
      }
  }

  private class PendQueue(mods: Collection<CddaParseMod>) {
    private val modTypeJsonsMap: MutableMap<CddaParseMod, MutableMap<CddaType, MutableSet<CddaParsedJson>>> =
      mutableMapOf()

    init {
      for (mod in mods) {
        for (cddaItem in mod.cddaParsedJsons) {
          val typeJsonsMap = modTypeJsonsMap.getOrElse(mod) {
            val newTypeItemsMap = mutableMapOf<CddaType, MutableSet<CddaParsedJson>>()
            modTypeJsonsMap[mod] = newTypeItemsMap
            newTypeItemsMap
          }
          typeJsonsMap.getOrElse(cddaItem.cddaType) {
            val newItems = mutableSetOf<CddaParsedJson>()
            typeJsonsMap[cddaItem.cddaType] = newItems
            newItems
          }.add(cddaItem)
        }
      }
    }

    fun pop(mod: CddaParseMod, type: CddaType): MutableSet<CddaParsedJson> {
      val cddaItems =
        modTypeJsonsMap.getOrDefault(mod, mutableMapOf()).getOrDefault(type, mutableSetOf()).toMutableSet()
      val result = cddaItems.toMutableSet()
      cddaItems.clear()
      return result
    }
  }

  private class FinalQueue {
    private val modTypeIdItemsMap: MutableMap<CddaParseMod, MutableMap<CddaType, MutableMap<String, MutableSet<CddaParseItem>>>> =
      mutableMapOf()

    fun add(cddaParseItem: CddaParseItem) {
      val typeIdItemsMap = modTypeIdItemsMap.getOrElse(cddaParseItem.mod) {
        val newTypeItemsMap = mutableMapOf<CddaType, MutableMap<String, MutableSet<CddaParseItem>>>()
        modTypeIdItemsMap[cddaParseItem.mod] = newTypeItemsMap
        newTypeItemsMap
      }
      val idItemsMap = typeIdItemsMap.getOrElse(cddaParseItem.cddaType) {
        val newIdItems = mutableMapOf<String, MutableSet<CddaParseItem>>()
        typeIdItemsMap[cddaParseItem.cddaType] = newIdItems
        newIdItems
      }
      idItemsMap.getOrElse(cddaParseItem.id) {
        val newItems = mutableSetOf<CddaParseItem>()
        idItemsMap[cddaParseItem.id] = newItems
        newItems
      }.add(cddaParseItem)
    }

    fun find(mod: CddaParseMod, type: CddaType, id: String): MutableSet<CddaParseItem> {
      mod.allDepMods.forEach {
        val cddaItems = modTypeIdItemsMap.getOrDefault(it, mutableMapOf()).getOrDefault(type, mutableMapOf())
          .getOrDefault(id, mutableSetOf())
        if (cddaItems.isNotEmpty()) return cddaItems
      }
      return mutableSetOf()
    }

    fun printStatus(log: Logger) {
      val finalItems = modTypeIdItemsMap.values.flatMap { it.values }.flatMap { it.values }.flatten()
      log.info("Final item size is ${finalItems.size}")
    }

    fun getModItemsSize(mod: CddaParseMod): Int {
      return getModItems(mod).size
    }

    fun getModItems(mod: CddaParseMod): List<CddaParseItem> {
      return modTypeIdItemsMap.getOrDefault(mod, mutableMapOf()).values.flatMap { it.values }.flatten()
    }
  }

  private class DeferQueue {
    val typeIdItemsMap: MutableMap<CddaType, MutableMap<String, MutableSet<CddaParseItem>>> =
      mutableMapOf()

    fun add(cddaParseItem: CddaParseItem, deferType: CddaType, deferId: String) {
      val idItemsMap = typeIdItemsMap.getOrElse(deferType) {
        val newIdItems = mutableMapOf<String, MutableSet<CddaParseItem>>()
        typeIdItemsMap[deferType] = newIdItems
        newIdItems
      }
      idItemsMap.getOrElse(deferId) {
        val newItems = mutableSetOf<CddaParseItem>()
        idItemsMap[deferId] = newItems
        newItems
      }.add(cddaParseItem)
    }

    fun pop(type: CddaType, id: String): MutableSet<CddaParseItem> {
      val cddaItems = typeIdItemsMap.getOrDefault(type, mutableMapOf()).getOrDefault(id, mutableSetOf())
      val result = cddaItems.toMutableSet()
      cddaItems.clear()
      return result
    }
  }
}
