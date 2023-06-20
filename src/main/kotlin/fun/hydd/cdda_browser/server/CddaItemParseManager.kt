package `fun`.hydd.cdda_browser.server

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.CddaModDto
import `fun`.hydd.cdda_browser.model.FinalCddaItem
import `fun`.hydd.cdda_browser.model.base.NeedDeferException
import `fun`.hydd.cdda_browser.model.base.ProcessContext
import `fun`.hydd.cdda_browser.model.base.parent.CddaObject
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import `fun`.hydd.cdda_browser.util.JsonUtil
import `fun`.hydd.cdda_browser.util.extension.proportional
import `fun`.hydd.cdda_browser.util.extension.relative
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

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

    cddaObjectClass.memberProperties.filterIsInstance<KMutableProperty<*>>().forEach { prop ->
      log.info("\t\t\tprocess prop: ${prop.name}")
      val mapInfo = prop.findAnnotations(MapInfo::class).firstOrNull() ?: MapInfo()
      if (!mapInfo.ignore) {
        val jsonFieldName = mapInfo.key.ifBlank { JsonUtil.javaField2JsonField(prop.name) }
        if (item.json.containsKey(jsonFieldName)) {
          val subJsonValue = item.json.getValue(jsonFieldName)
          if (subJsonValue == null)
            prop.setter.call(instant, null)
          else
            prop.setter.call(
              instant, JsonUtil.parseJsonField(prop.returnType, item.json.getValue(jsonFieldName), mapInfo.param)
            )
        }
        if (parent != null) processCopyFrom(prop, item, jsonFieldName, mapInfo.param, instant)
      }
      if (mapInfo.spFun.isNotBlank()) {
        val spFun = cddaObjectClass.functions.firstOrNull() { it.name == mapInfo.spFun }
          ?: throw Exception("class $cddaObjectClass spFun ${mapInfo.spFun} is miss")
        spFun.call(instant)
      }
    }
    return instant
  }

  @Suppress("UNCHECKED_CAST")
  private fun processCopyFrom(
    prop: KMutableProperty<*>,
    item: CddaCommonItem,
    jsonFieldName: String,
    param: String,
    instant: CddaObject,
  ) {
    val fieldType = prop.returnType
    val fieldClass = fieldType.jvmErasure
    if (fieldClass.superclasses.contains(MutableCollection::class)) {
      if (item.extend?.containsKey(jsonFieldName) == true) {
        val currentValue = prop.getter.call(instant) as MutableCollection<Any>
        val extendValue = JsonUtil.parseJsonField(
          fieldType,
          item.extend.getValue(jsonFieldName),
          param
        ) as MutableCollection<Any>
        currentValue.addAll(extendValue)
      }

      if (item.delete?.containsKey(jsonFieldName) == true) {
        val currentValue = prop.getter.call(instant) as MutableCollection<*>
        val deleteValue = JsonUtil.parseJsonField(
          fieldType,
          item.delete.getValue(jsonFieldName),
          param
        ) as MutableCollection<*>
        currentValue.removeAll(deleteValue.toSet())
      }
    } else {
      if (item.relative?.containsKey(jsonFieldName) == true) {
        val currentValue = prop.getter.call(instant)
        val relativeJsonValue = item.relative.getValue(jsonFieldName)
        val relativeValue =
          JsonUtil.parseJsonField(fieldType, relativeJsonValue, param)
        if (currentValue is CddaSubObject && relativeValue is CddaSubObject) {
          currentValue.relative(relativeJsonValue, param)
        } else if (currentValue is Double && relativeValue is Double) {
          val result = Double.relative(relativeJsonValue, currentValue)
          prop.setter.call(instant, result)
        } else if (currentValue is Int && relativeValue is Int) {
          val result = Int.relative(relativeJsonValue, currentValue)
          prop.setter.call(instant, result)
        } else throw Exception("fieldClass(${fieldClass.jvmName}) not baseType or CddaSubObject")
      }

      if (item.proportional?.containsKey(jsonFieldName) == true) {
        val currentValue = prop.getter.call(instant)
        val proportionalJsonValue = item.proportional.getValue(jsonFieldName)
        val proportionalValue =
          JsonUtil.parseJsonField(fieldType, proportionalJsonValue, param)
        if (currentValue is CddaSubObject && proportionalValue is CddaSubObject) {
          currentValue.proportional(proportionalJsonValue, param)
        } else if (currentValue is Double && proportionalValue is Double) {
          val result = Double.proportional(proportionalJsonValue, currentValue)
          prop.setter.call(instant, result)
        } else if (currentValue is Int && proportionalValue is Int) {
          val result = Int.proportional(proportionalJsonValue, currentValue)
          prop.setter.call(instant, result)
        } else throw Exception("fieldClass(${fieldClass.jvmName}) not baseType or CddaSubObject")
      }
    }
  }
}
