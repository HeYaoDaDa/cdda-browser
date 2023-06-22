package `fun`.hydd.cdda_browser.server

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.CddaModDto
import `fun`.hydd.cdda_browser.model.FinalCddaItem
import `fun`.hydd.cdda_browser.model.InheritData
import `fun`.hydd.cdda_browser.model.base.NeedDeferException
import `fun`.hydd.cdda_browser.model.base.ProcessContext
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.util.JsonUtil
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import kotlin.reflect.full.primaryConstructor

object CddaItemParseManager {
  private val log = LoggerFactory.getLogger(this.javaClass)

  suspend fun process(fileSystem: FileSystem, cddaMods: List<CddaModDto>): List<FinalCddaItem> {
    cddaMods.forEach { processByCddaMod(fileSystem, it) }
    return ProcessContext.finalManager.popResult()
  }

  private suspend fun processByCddaMod(fileSystem: FileSystem, cddaMod: CddaModDto) {
    log.info("mod ${cddaMod.id} start")
    ProcessContext.mod = cddaMod
    val pendMap = getPendMap(fileSystem, cddaMod)
    CddaType.values().filter { it != CddaType.NULL }.forEach { cddaType ->
      log.info("\tCddaType $cddaType start")
      val itemPairList = pendMap.getOrDefault(cddaType, listOf()).flatMap { parseId(it, cddaType) }
      for (itemPair in itemPairList) {
        val id = itemPair.first
        val commonItem = itemPair.second
        processCddaItem(commonItem, id)
      }
      log.info("\tCddaType $cddaType end")
    }
    ProcessContext.deferManager.check()
    ProcessContext.mod = null
    log.info("mod ${cddaMod.id} end")
  }

  private fun processCddaItem(commonItem: CddaCommonItem, id: String) {
    val cddaType = commonItem.cddaType
    val itemRef = CddaItemRef(cddaType, id)
    log.info("\t\tCddaItem ${itemRef.type}/${itemRef.id} start")
    ProcessContext.commonItem = commonItem
    ProcessContext.itemId = id
    try {
      val cddaObject = parse(commonItem, itemRef)
      val finalItem = FinalCddaItem(
        commonItem,
        cddaObject,
        id,
        cddaType,
        commonItem.jsonType,
        commonItem.path,
        commonItem.abstract,
        cddaObject.itemName,
        cddaObject.itemDescription,
        commonItem.json
      )
      ProcessContext.finalManager.add(finalItem)
      ProcessContext.commonItem = null
      ProcessContext.itemId = null
      log.info("\t\tCddaItem ${itemRef.type}/${itemRef.id} end")
      ProcessContext.deferManager.pop(itemRef).forEach { deferCddaItem ->
        log.info("\t\tdeferCddaItem ${deferCddaItem.second.cddaType}/${deferCddaItem.first}")
        processCddaItem(deferCddaItem.second, deferCddaItem.first)
      }
    } catch (e: NeedDeferException) {
      ProcessContext.deferManager.add(e.defer, commonItem, id)
      log.info("\t\tparse fail need:${e.defer}")
    }
  }

  private fun parse(
    commonItem: CddaCommonItem,
    itemRef: CddaItemRef
  ): CddaObject {
    var parent: CddaObject? = null
    if (commonItem.copyFrom != null) {
      val parentItemRef = CddaItemRef(commonItem.cddaType, commonItem.copyFrom)
      val parseFinalItem = ProcessContext.FinalManager.find(parentItemRef)
      parent = parseFinalItem.cddaObject
    }
    val cddaObject = mapCddaCommonItem(commonItem, parent)
    cddaObject.finalize(commonItem, itemRef)
    return cddaObject
  }

  private fun parseId(cddaCommonItem: CddaCommonItem, cddaType: CddaType): List<Pair<String, CddaCommonItem>> {
    val ids: Set<String> = if (cddaCommonItem.json.containsKey("abstract"))
      setOf(cddaCommonItem.json.getString("abstract"))
    else
      cddaType.getIdsFun.call(cddaCommonItem)
    if (ids.isEmpty()) throw Exception("get id is empty")
    return ids.map { Pair(it, cddaCommonItem) }
  }

  private suspend fun getPendMap(
    fileSystem: FileSystem,
    cddaMod: CddaModDto
  ): MutableMap<CddaType, MutableList<CddaCommonItem>> {
    val pendMap = mutableMapOf<CddaType, MutableList<CddaCommonItem>>()
    getCddaCommonItemByMod(fileSystem, cddaMod, mutableSetOf()).forEach {
      val items = pendMap.getOrDefault(it.cddaType, mutableListOf())
      items.add(it)
      pendMap[it.cddaType] = items
    }
    return pendMap
  }

  private suspend fun getCddaCommonItemByMod(
    fileSystem: FileSystem,
    mod: CddaModDto,
    missJsonTypes: MutableSet<String>
  ) =
    coroutineScope {
      mod.path.flatMap { getAllJsonFileInDir(it.toFile()) }.map { jsonFile ->
        async {
          JsonUtil.getJsonObjectsByFile(fileSystem, jsonFile)
            .map { convertCddaCommonItem(it, mod, jsonFile.toPath(), missJsonTypes) }
        }
      }.awaitAll().flatten().filterNotNull().toSet()
    }

  private fun convertCddaCommonItem(
    jsonObject: JsonObject,
    mod: CddaModDto,
    path: Path,
    missJsonTypes: MutableSet<String>
  ): CddaCommonItem? {
    return if (jsonObject.containsKey("type")) {
      val type = jsonObject.getString("type")
      if (missJsonTypes.contains(type)) return null
      var cddaType: CddaType? = null
      var jsonType: JsonType? = null
      for (cddaTypeEntry in CddaType.values()) {
        for (jsonTypeEntry in cddaTypeEntry.jsonType) {
          if (jsonTypeEntry.isEquals(type)) {
            cddaType = cddaTypeEntry
            jsonType = jsonTypeEntry
            break
          }
        }
      }
      if (cddaType != null && jsonType != null) {
        CddaCommonItem(
          jsonType,
          cddaType,
          mod,
          path,
          jsonObject,
          jsonObject.getString("copy-from"),
          jsonObject.containsKey("abstract"),
          jsonObject.getJsonObject("relative"),
          jsonObject.getJsonObject("proportional"),
          jsonObject.getJsonObject("extend"),
          jsonObject.getJsonObject("delete"),
        )
      } else {
        missJsonTypes.add(type)
        null
      }
    } else {
      null
    }
  }

  private suspend fun getAllJsonFileInDir(dirPath: File): List<File> = coroutineScope {
    val result: MutableList<File> = ArrayList()
    val dirFiles = dirPath.listFiles()
    if (dirFiles != null && dirFiles.isNotEmpty()) {
      dirFiles.mapNotNull {
        async {
          if (it.isDirectory) result.addAll(getAllJsonFileInDir(it))
          else if (it.absolutePath.endsWith(".json")) result.add(it)
          else null
        }
      }.awaitAll()
    }
    result
  }

  private fun mapCddaCommonItem(
    item: CddaCommonItem,
    parent: CddaObject?
  ): CddaObject {
    val cddaObjectClass = item.cddaType.objectClass
    val instant = parent ?: cddaObjectClass.primaryConstructor!!.callBy(emptyMap())

    JsonUtil.autoLoad(instant, item.json, InheritData(item.relative, item.proportional, item.extend, item.delete))
    return instant
  }
}
