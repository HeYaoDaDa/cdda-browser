package `fun`.hydd.cdda_browser.server

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.dto.CddaItem
import `fun`.hydd.cdda_browser.dto.CddaJson
import `fun`.hydd.cdda_browser.dto.CddaModDto
import `fun`.hydd.cdda_browser.entity.CddaVersion
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.hibernate.reactive.stage.Stage
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CddaItemParseManager(
  private val version: CddaVersion,
  private val mods: Collection<CddaModDto>
) {
  private val pendQueue: PendQueue = PendQueue(mods)
  private val finalQueue: FinalQueue = FinalQueue()
  private val log = LoggerFactory.getLogger(this.javaClass)

  private class PendQueue(mods: Collection<CddaModDto>) {
    private val modTypeJsonsMap: MutableMap<CddaModDto, MutableMap<CddaType, MutableSet<CddaJson>>> = mutableMapOf()

    init {
      for (mod in mods) {
        for (cddaItem in mod.cddaJsons) {
          val typeJsonsMap = modTypeJsonsMap.getOrElse(mod) {
            val newTypeItemsMap = mutableMapOf<CddaType, MutableSet<CddaJson>>()
            modTypeJsonsMap[mod] = newTypeItemsMap
            newTypeItemsMap
          }
          typeJsonsMap.getOrElse(cddaItem.cddaType) {
            val newItems = mutableSetOf<CddaJson>()
            typeJsonsMap[cddaItem.cddaType] = newItems
            newItems
          }.add(cddaItem)
        }
      }
    }

    fun pop(mod: CddaModDto, type: CddaType): MutableSet<CddaJson> {
      val cddaItems =
        modTypeJsonsMap.getOrDefault(mod, mutableMapOf()).getOrDefault(type, mutableSetOf()).toMutableSet()
      val result = cddaItems.toMutableSet()
      cddaItems.clear()
      return result
    }
  }

  private class FinalQueue {
    private val modTypeIdItemsMap: MutableMap<CddaModDto, MutableMap<CddaType, MutableMap<String, MutableSet<CddaItem>>>> =
      mutableMapOf()

    fun add(cddaItem: CddaItem) {
      val typeIdItemsMap = modTypeIdItemsMap.getOrElse(cddaItem.mod) {
        val newTypeItemsMap = mutableMapOf<CddaType, MutableMap<String, MutableSet<CddaItem>>>()
        modTypeIdItemsMap[cddaItem.mod] = newTypeItemsMap
        newTypeItemsMap
      }
      val idItemsMap = typeIdItemsMap.getOrElse(cddaItem.cddaType) {
        val newIdItems = mutableMapOf<String, MutableSet<CddaItem>>()
        typeIdItemsMap[cddaItem.cddaType] = newIdItems
        newIdItems
      }
      idItemsMap.getOrElse(cddaItem.id) {
        val newItems = mutableSetOf<CddaItem>()
        idItemsMap[cddaItem.id] = newItems
        newItems
      }.add(cddaItem)
    }

    fun find(mod: CddaModDto, type: CddaType, id: String): MutableSet<CddaItem> {
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

    fun getModItemsSize(mod: CddaModDto): Int {
      return getModItems(mod).size
    }

    fun getModItems(mod: CddaModDto): List<CddaItem> {
      return modTypeIdItemsMap.getOrDefault(mod, mutableMapOf()).values.flatMap { it.values }.flatten()
    }
  }

  private class DeferQueue {
    val typeIdItemsMap: MutableMap<CddaType, MutableMap<String, MutableSet<CddaItem>>> =
      mutableMapOf()

    fun add(cddaItem: CddaItem, deferType: CddaType, deferId: String) {
      val idItemsMap = typeIdItemsMap.getOrElse(deferType) {
        val newIdItems = mutableMapOf<String, MutableSet<CddaItem>>()
        typeIdItemsMap[deferType] = newIdItems
        newIdItems
      }
      idItemsMap.getOrElse(deferId) {
        val newItems = mutableSetOf<CddaItem>()
        idItemsMap[deferId] = newItems
        newItems
      }.add(cddaItem)
    }

    fun pop(type: CddaType, id: String): MutableSet<CddaItem> {
      val cddaItems = typeIdItemsMap.getOrDefault(type, mutableMapOf()).getOrDefault(id, mutableSetOf())
      val result = cddaItems.toMutableSet()
      cddaItems.clear()
      return result
    }
  }

  suspend fun parseAll(factory: Stage.SessionFactory) = coroutineScope {
    val result = mods.map { mod ->
      val deferQueue = DeferQueue()
      deferQueue.typeIdItemsMap.clear()
      for (cddaType in CddaType.values()) {
        val cddaJsons = pendQueue.pop(mod, cddaType)
        val cddaItems = cddaJsons.map { async { convertCddaItems(it) } }.awaitAll().flatten()
        parseCddaItems(deferQueue, cddaItems, mod)
      }
      val deferCddaItem = deferQueue.typeIdItemsMap.values.map { it.values.flatten() }.flatten()
      log.info(
        "mod ${mod.modId} defer item size is ${deferCddaItem.size}, final size is ${finalQueue.getModItemsSize(mod)}"
      )
      if (deferCddaItem.isNotEmpty()) {
        deferCddaItem.forEach {
          log.warn("${it.cddaType} : ${it.id} is defer")
        }
        throw Exception("Has defer CddaI tem")
      }
      mod.toEntity(factory, finalQueue.getModItems(mod))
    }
    version.cddaMods = result.toMutableSet()
    result.forEach { it.cddaVersion = version }
    finalQueue.printStatus(log)
  }

  private fun parseCddaItems(
    deferQueue: DeferQueue,
    cddaItems: Collection<CddaItem>,
    mod: CddaModDto
  ) {
    for (cddaItem in cddaItems) {
      val parser = cddaItem.cddaType.parser
      if (cddaItem.copyFrom != null) {
        val parentItem = finalQueue.find(mod, cddaItem.cddaType, cddaItem.copyFrom).firstOrNull()
        if (parentItem != null) {
          val data = parentItem.data!!
          val ref = parser.parse(cddaItem, data)
          if (ref !== null) deferQueue.add(cddaItem, ref.type, ref.id)
          else {
            finalQueue.add(cddaItem)
            val deferItems = deferQueue.pop(cddaItem.cddaType, cddaItem.id)
            if (deferItems.isNotEmpty()) parseCddaItems(deferQueue, deferItems, mod)
          }
        } else {
          deferQueue.add(cddaItem, cddaItem.cddaType, cddaItem.copyFrom)
        }
      } else {
        val ref = parser.parse(cddaItem, null)
        if (ref !== null) deferQueue.add(cddaItem, ref.type, ref.id)
        else {
          finalQueue.add(cddaItem)
          val deferItems = deferQueue.pop(cddaItem.cddaType, cddaItem.id)
          if (deferItems.isNotEmpty()) parseCddaItems(deferQueue, deferItems, mod)
        }
      }
    }
  }

  private fun convertCddaItems(cddaJson: CddaJson): List<CddaItem> {
    val parser = cddaJson.cddaType.parser
    val ids = parser.parseIds(cddaJson)
    if (ids.isEmpty()) throw Exception("Parse id is empty")
    return ids
      .map {
        CddaItem(
          cddaJson.jsonType,
          cddaJson.cddaType,
          it,
          cddaJson.mod,
          cddaJson.path,
          cddaJson.json,
          cddaJson.copyFrom,
          cddaJson.abstract,
          cddaJson.relative,
          cddaJson.proportional,
          cddaJson.extend,
          cddaJson.delete
        )
      }
  }
}
