package `fun`.hydd.cdda_browser.server

import `fun`.hydd.cdda_browser.annotation.CddaProperty
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.model.*
import `fun`.hydd.cdda_browser.model.base.CddaItemRef
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemData
import `fun`.hydd.cdda_browser.model.jsonParser.JsonParser
import `fun`.hydd.cdda_browser.util.JsonUtil
import `fun`.hydd.cdda_browser.util.extension.getCollection
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.jvmErasure

object CddaItemParseManager {
  private val log = LoggerFactory.getLogger(this.javaClass)

  private val finalQueue = FinalQueue()
  private val dependMap = mutableMapOf<CddaItemRef, MutableSet<CddaItemRef>>()

  suspend fun process(fileSystem: FileSystem, cddaMods: List<CddaModDto>): List<FinalCddaItem> {
    cddaMods.forEach { processByCddaMod(fileSystem, it) }
    return finalQueue.popResult()
  }

  private suspend fun processByCddaMod(fileSystem: FileSystem, cddaMod: CddaModDto) {
    log.info("process mod ${cddaMod.id} start")
    val allDepend = cddaMod.allDependencies.toMutableList()
    allDepend.add(cddaMod)
    val modOrder = ModOrder(allDepend)
    val pendMap = getPendMap(fileSystem, cddaMod)
    val deferQueue = DeferQueue()
    CddaType.values().forEach { cddaType ->
      log.info("process CddaType $cddaType start")
      val itemPairList = pendMap.getOrDefault(cddaType, listOf()).flatMap { parseId(it, cddaType) }
      for (itemPair in itemPairList) {
        val id = itemPair.first
        val commonItem = itemPair.second
        processCddaItem(id, commonItem, null, null, modOrder, deferQueue)
      }
      log.info("process CddaType $cddaType end")
    }
    deferQueue.check()
    log.info("process mod ${cddaMod.id} end")
  }

  private fun processCddaItem(
    id: String,
    commonItem: CddaCommonItem,
    oldJsonEntity: Any?,
    oldDependencies: MutableMap<CddaItemRef, ModOrder>?,
    modOrder: ModOrder,
    deferQueue: DeferQueue
  ) {
    val cddaType = commonItem.cddaType
    val itemRef = CddaItemRef(cddaType, id)
    log.info("process CddaItem $itemRef, hasOldJsonEntity: ${oldJsonEntity != null} start")
    val jsonEntity: Any
    val dependencies: MutableMap<CddaItemRef, ModOrder>
    if (oldJsonEntity == null || oldDependencies == null) {
      val parseResult = parse(commonItem, modOrder)
      if (!parseResult.isPass()) {
        deferQueue.add(parseResult.deferRef!!, commonItem, modOrder, id)
        log.info("parse fail need:${parseResult.deferRef}")
        return
      }
      jsonEntity = parseResult.jsonEntity!!
      dependencies = parseResult.dependencies!!
    } else {
      jsonEntity = oldJsonEntity
      dependencies = oldDependencies
    }
    val finalResult = commonItem.cddaType.parser.parse(jsonEntity, dependencies)
    if (!finalResult.isPass()) {
      deferQueue.add(
        finalResult.deferRef!!,
        commonItem,
        jsonEntity,
        dependencies,
        modOrder,
        id
      )
      log.info("final fail need:$dependencies")
      return
    }
    finalResult.dependencies!!.map { it.key }.forEach { depend ->
      val dependList = dependMap.getOrElse(depend) {
        val newSet = mutableSetOf<CddaItemRef>()
        dependMap[depend] = newSet
        newSet
      }
      dependList.add(depend)
    }
    val finalItem = FinalCddaItem(
      commonItem,
      jsonEntity,
      finalResult.cddaItemData!!,
      finalResult.dependencies,
      id,
      modOrder,
      cddaType,
      commonItem.jsonType,
      commonItem.path,
      commonItem.abstract,
      finalResult.cddaItemData.itemName,
      finalResult.cddaItemData.itemDescription,
      commonItem.json
    )
    deferQueue.pop(itemRef).forEach { deferCddaItem ->
      log.info("resume deferCddaItem $deferCddaItem")
      if (deferCddaItem.jsonEntity == null || deferCddaItem.dependencies == null) {
        processCddaItem(deferCddaItem.id, deferCddaItem.commonItem, null, null, deferCddaItem.modOrder, deferQueue)
      } else {
        processCddaItem(
          deferCddaItem.id,
          deferCddaItem.commonItem,
          deferCddaItem.jsonEntity,
          deferCddaItem.dependencies,
          deferCddaItem.modOrder,
          deferQueue
        )
      }
    }
    (dependMap[itemRef] ?: mutableListOf()).forEach { updateItemRef ->
      val updateFinalItem = finalQueue.find(updateItemRef, modOrder)
      if (updateFinalItem != null) {
        if (updateFinalItem.modOrder == modOrder) {
          log.info("update CddaItem ${updateItemRef}, modOrder: $modOrder")
          val updateCddaItem =
            updateCddaItem(updateFinalItem.cddaCommonItem, updateFinalItem.modOrder)
          updateFinalItem.jsonEntity = updateCddaItem.first
          updateFinalItem.cddaItemData = updateCddaItem.second
          updateFinalItem.dependencies = updateCddaItem.third
        } else if (updateFinalItem.id == id) {
          log.info("skip update self.")
        } else {
          log.info("update add CddaItem ${updateItemRef}, modOrder: $modOrder")
          processCddaItem(updateFinalItem.id, updateFinalItem.cddaCommonItem, null, null, modOrder, deferQueue)
        }
      } else {
        throw Exception("not find final item")
      }
    }
    finalQueue.add(finalItem)
    // fill miss modOrder
    val existModOrders = finalQueue.findModOrders(itemRef)
    ModOrder.getAllMissModOrder(existModOrders).forEach {
      log.info("add miss CddaItem ${itemRef}, modOrder: $it")
      processCddaItem(id, commonItem, null, null, it, deferQueue)
    }
    log.info("process CddaItem $itemRef, hasOldJsonEntity: ${oldJsonEntity != null} end")
  }

  private fun updateCddaItem(
    commonItem: CddaCommonItem,
    modOrder: ModOrder,
  ): Triple<Any, CddaItemData, MutableMap<CddaItemRef, ModOrder>> {
    log.info("update CddaItem start")
    val parseResult = parse(commonItem, modOrder)
    if (!parseResult.isPass()) {
      throw Exception("update not such not pass")
    }
    val jsonEntity = parseResult.jsonEntity!!
    val dependencies = parseResult.dependencies!!
    val finalResult = commonItem.cddaType.parser.parse(jsonEntity, dependencies)
    if (!finalResult.isPass()) {
      throw Exception("update not such not pass")
    }
    log.info("update CddaItem end")
    return Triple(parseResult.jsonEntity, finalResult.cddaItemData!!, finalResult.dependencies!!)
  }

  private fun parse(
    commonItem: CddaCommonItem,
    modOrder: ModOrder,
  ): ParseResult {
    var parentJsonEntity: Any? = null
    var dependencies: MutableMap<CddaItemRef, ModOrder> = mutableMapOf()
    if (commonItem.copyFrom != null) {
      val parentItemRef = CddaItemRef(commonItem.cddaType, commonItem.copyFrom)
      val parseFinalItem = finalQueue.find(parentItemRef, modOrder) ?: return ParseResult(null, null, parentItemRef)
      dependencies = mutableMapOf(Pair(parentItemRef, parseFinalItem.modOrder))
      parentJsonEntity = parseFinalItem.jsonEntity
    }
    val jsonEntity = parseCddaCommonItem(commonItem, parentJsonEntity)
    return ParseResult(jsonEntity, dependencies, null)
  }

  private fun parseId(cddaCommonItem: CddaCommonItem, cddaType: CddaType): List<Pair<String, CddaCommonItem>> {
    val parser = cddaType.parser
    val ids: Set<String> = if (cddaCommonItem.json.containsKey("abstract"))
      setOf(cddaCommonItem.json.getString("abstract"))
    else
      parser.parseIds(cddaCommonItem)
    if (ids.isEmpty()) throw Exception("Parse id is empty")
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

  private fun parseCddaCommonItem(
    item: CddaCommonItem,
    parent: Any?
  ): Any {
    val jsonEntityClass = item.cddaType.jsonEntity
    val instant = parent ?: jsonEntityClass.primaryConstructor!!.call()

    jsonEntityClass.memberProperties.filterIsInstance<KMutableProperty<*>>().forEach { prop ->
      val propAnnotation = prop.findAnnotations(CddaProperty::class).firstOrNull()
      val annotationPara = propAnnotation?.para ?: ""
      val jsonField =
        if (propAnnotation != null && propAnnotation.key.isNotBlank()) propAnnotation.key else javaField2JsonField(prop.name)
      val propClass = prop.returnType.jvmErasure
      if (propClass.superclasses.contains(MutableCollection::class)) {
        val subPropClass = prop.returnType.arguments[0].type!!.jvmErasure
        val jsonParser = JsonParser.jsonParsers[subPropClass] ?: throw Exception("miss jsonEntity Type: $propClass")
        if (item.json.containsKey(jsonField)) {
          prop.setter.call(
            instant,
            item.json.getCollection<Any>(jsonField)!!.map { jsonParser.parse(it, annotationPara) })
        }
        if (parent != null) {
          if (item.extend?.containsKey(jsonField) == true) {
            val oldValue = (prop.getter.call())!! as MutableCollection<Any>
            oldValue.add(jsonParser.parse(item.extend.getValue(jsonField), annotationPara))
          }
          if (item.delete?.containsKey(jsonField) == true) {
            val oldValue = prop.getter.call()!! as MutableCollection<Any>
            oldValue.remove(jsonParser.parse(item.delete.getValue(jsonField), annotationPara))
          }
        }
      } else {
        val jsonParser = JsonParser.jsonParsers[propClass] ?: throw Exception("miss jsonEntity Type: $propClass")
        if (item.json.containsKey(jsonField)) {
          prop.setter.call(instant, jsonParser.parse(item.json.getValue(jsonField), annotationPara))
        }
        if (parent != null) {
          if (item.relative?.containsKey(jsonField) == true) {
            prop.setter.call(
              instant,
              jsonParser.relative(
                prop.getter.call()!!,
                jsonParser.parse(item.relative.getValue(jsonField), annotationPara)
              )
            )
          }
          if (item.proportional?.containsKey(jsonField) == true) {
            prop.setter.call(
              instant,
              jsonParser.proportional(
                prop.getter.call()!!,
                jsonParser.parse(item.proportional.getValue(jsonField), annotationPara)
              )
            )
          }
        }
      }
    }
    return instant
  }

  private fun javaField2JsonField(fieldName: String): String {
    val result = StringBuilder()
    for (char in fieldName) {
      if (char != char.lowercaseChar()) result.append("_")
      result.append(char.lowercase())
    }
    return result.toString()
  }

  private class FinalQueue {
    private val value: MutableMap<CddaItemRef, MutableMap<ModOrder, FinalCddaItem>> = mutableMapOf()

    fun popResult(): List<FinalCddaItem> {
      val result = value.flatMap { it.value.values }
      value.clear()
      return result
    }

    fun add(finalCddaItem: FinalCddaItem) {
      val cddaItemRef = CddaItemRef(finalCddaItem.cddaType, finalCddaItem.id)
      val modOrderAndFinalCddaItemMap = value.getOrElse(cddaItemRef) {
        val newMap = mutableMapOf<ModOrder, FinalCddaItem>()
        value[cddaItemRef] = newMap
        newMap
      }
      modOrderAndFinalCddaItemMap[finalCddaItem.modOrder] = finalCddaItem
    }

    fun findModOrders(cddaItemRef: CddaItemRef): Set<ModOrder> {
      val modOrderAndFinalCddaItemMap = value[cddaItemRef] ?: return emptySet()
      return modOrderAndFinalCddaItemMap.keys.toSet()
    }

    fun find(cddaItemRef: CddaItemRef, modOrder: ModOrder): FinalCddaItem? {
      val modOrderAndFinalCddaItemMap = value[cddaItemRef] ?: return null
      return modOrderAndFinalCddaItemMap.filter { modOrder.contains(it.key) }.maxBy { it.key.value.size }.value
    }
  }

  private class DeferQueue {
    private val value: MutableMap<CddaItemRef, MutableList<DeferCddaItem>> = mutableMapOf()

    fun add(deferRef: CddaItemRef, commonItem: CddaCommonItem, modOrder: ModOrder, id: String) {
      val deferItems = value.getOrElse(deferRef) {
        val newList = mutableListOf<DeferCddaItem>()
        value[deferRef] = newList
        newList
      }
      deferItems.add(DeferCddaItem(commonItem, null, null, modOrder, id))
    }

    fun add(
      deferRef: CddaItemRef,
      commonItem: CddaCommonItem,
      jsonEntity: Any,
      dependencies: MutableMap<CddaItemRef, ModOrder>,
      modOrder: ModOrder,
      id: String
    ) {
      val deferItems = value.getOrElse(deferRef) {
        val newList = mutableListOf<DeferCddaItem>()
        value[deferRef] = newList
        newList
      }
      deferItems.add(DeferCddaItem(commonItem, jsonEntity, dependencies, modOrder, id))
    }

    fun pop(ref: CddaItemRef): List<DeferCddaItem> {
      return value[ref] ?: listOf()
    }

    fun check() {
      val filed = value.filter { it.value.isNotEmpty() }
      if (filed.isNotEmpty()) {
        filed.forEach { entry ->
          entry.value.forEach {
            log.warn("item ${it.commonItem.cddaType}/${it.id} miss depend ${entry.key}")
          }
        }
        throw Exception("not clear defer map")
      }
    }
  }
}
