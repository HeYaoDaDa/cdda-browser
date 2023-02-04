package `fun`.hydd.cdda_browser.verticles

import com.googlecode.jmapper.JMapper
import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.EventBusConstant
import `fun`.hydd.cdda_browser.model.base.CddaItemParseDto
import `fun`.hydd.cdda_browser.model.base.CddaJsonParseDto
import `fun`.hydd.cdda_browser.model.base.CddaModParseDto
import `fun`.hydd.cdda_browser.model.base.CddaVersionParseDto
import `fun`.hydd.cdda_browser.model.base.parent.CddaItemParser
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CddaItemParserVerticle : CoroutineVerticle() {
  private val log = LoggerFactory.getLogger(this.javaClass)

  override suspend fun start() {
    super.start()
    val eventBus = vertx.eventBus()
    eventBus.consumer<JsonObject>(EventBusConstant.CDDA_ITEM_PARSER_GET_TAG_LIST) {
      val cddaVersionParseDto = it.body().mapTo(CddaVersionParseDto::class.java)
      parseCddaVersion(cddaVersionParseDto)
      it.reply(JsonObject.mapFrom(cddaVersionParseDto))
    }
    log.info("CddaItemParserVerticle Start End")
  }

  private fun parseCddaVersion(cddaVersionParseDto: CddaVersionParseDto) {
    val pendQueue = PendQueue(cddaVersionParseDto.cddaMods)
    val finalQueue = FinalQueue()
    cddaVersionParseDto.cddaMods.forEach { mod ->
      val deferQueue = DeferQueue()
      for (cddaType in CddaType.values()) {
        val cddaItems = pendQueue.pop(mod, cddaType).flatMap { parseId(it) }
        mod.cddaItems.addAll(cddaItems)
        parseCddaItems(finalQueue, deferQueue, mod, cddaItems)
      }
      // print defer status
      val deferCddaItem = deferQueue.typeIdItemsMap.values.map { it.values.flatten() }.flatten()
      log.info(
        "mod ${mod.modId} defer item size is ${deferCddaItem.size}, final size is ${finalQueue.getModItemsSize(mod)}"
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
    mod: CddaModParseDto,
    cddaItemParseDtos: Collection<CddaItemParseDto>
  ) {
    for (cddaItem in cddaItemParseDtos) {
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
    mod: CddaModParseDto,
    cddaItem: CddaItemParseDto,
    parser: CddaItemParser,
    parentItem: CddaItemParseDto?
  ) {
    val ref = parser.parse(cddaItem, parentItem?.data)
    if (ref !== null) deferQueue.add(cddaItem, ref.type, ref.id)
    else {
      finalQueue.add(cddaItem)
      val deferItems = deferQueue.pop(cddaItem.cddaType, cddaItem.id)
      if (deferItems.isNotEmpty()) parseCddaItems(finalQueue, deferQueue, mod, deferItems)
    }
  }

  private fun parseId(cddaJsonParseDto: CddaJsonParseDto): List<CddaItemParseDto> {
    val parser = cddaJsonParseDto.cddaType.parser
    val ids = parser.parseIds(cddaJsonParseDto)
    if (ids.isEmpty()) throw Exception("Parse id is empty")
    val jMapper = JMapper(CddaItemParseDto::class.java, CddaJsonParseDto::class.java)
    return ids
      .map {
        val cddaItemDto = jMapper.getDestination(cddaJsonParseDto)
        cddaItemDto.id = it
        cddaItemDto
      }
  }

  private class PendQueue(mods: Collection<CddaModParseDto>) {
    private val modTypeJsonsMap: MutableMap<CddaModParseDto, MutableMap<CddaType, MutableSet<CddaJsonParseDto>>> =
      mutableMapOf()

    init {
      for (mod in mods) {
        for (cddaItem in mod.cddaJsonParseDtos) {
          val typeJsonsMap = modTypeJsonsMap.getOrElse(mod) {
            val newTypeItemsMap = mutableMapOf<CddaType, MutableSet<CddaJsonParseDto>>()
            modTypeJsonsMap[mod] = newTypeItemsMap
            newTypeItemsMap
          }
          typeJsonsMap.getOrElse(cddaItem.cddaType) {
            val newItems = mutableSetOf<CddaJsonParseDto>()
            typeJsonsMap[cddaItem.cddaType] = newItems
            newItems
          }.add(cddaItem)
        }
      }
    }

    fun pop(mod: CddaModParseDto, type: CddaType): MutableSet<CddaJsonParseDto> {
      val cddaItems =
        modTypeJsonsMap.getOrDefault(mod, mutableMapOf()).getOrDefault(type, mutableSetOf()).toMutableSet()
      val result = cddaItems.toMutableSet()
      cddaItems.clear()
      return result
    }
  }

  private class FinalQueue {
    private val modTypeIdItemsMap: MutableMap<CddaModParseDto, MutableMap<CddaType, MutableMap<String, MutableSet<CddaItemParseDto>>>> =
      mutableMapOf()

    fun add(cddaItemParseDto: CddaItemParseDto) {
      val typeIdItemsMap = modTypeIdItemsMap.getOrElse(cddaItemParseDto.mod) {
        val newTypeItemsMap = mutableMapOf<CddaType, MutableMap<String, MutableSet<CddaItemParseDto>>>()
        modTypeIdItemsMap[cddaItemParseDto.mod] = newTypeItemsMap
        newTypeItemsMap
      }
      val idItemsMap = typeIdItemsMap.getOrElse(cddaItemParseDto.cddaType) {
        val newIdItems = mutableMapOf<String, MutableSet<CddaItemParseDto>>()
        typeIdItemsMap[cddaItemParseDto.cddaType] = newIdItems
        newIdItems
      }
      idItemsMap.getOrElse(cddaItemParseDto.id) {
        val newItems = mutableSetOf<CddaItemParseDto>()
        idItemsMap[cddaItemParseDto.id] = newItems
        newItems
      }.add(cddaItemParseDto)
    }

    fun find(mod: CddaModParseDto, type: CddaType, id: String): MutableSet<CddaItemParseDto> {
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

    fun getModItemsSize(mod: CddaModParseDto): Int {
      return getModItems(mod).size
    }

    fun getModItems(mod: CddaModParseDto): List<CddaItemParseDto> {
      return modTypeIdItemsMap.getOrDefault(mod, mutableMapOf()).values.flatMap { it.values }.flatten()
    }
  }

  private class DeferQueue {
    val typeIdItemsMap: MutableMap<CddaType, MutableMap<String, MutableSet<CddaItemParseDto>>> =
      mutableMapOf()

    fun add(cddaItemParseDto: CddaItemParseDto, deferType: CddaType, deferId: String) {
      val idItemsMap = typeIdItemsMap.getOrElse(deferType) {
        val newIdItems = mutableMapOf<String, MutableSet<CddaItemParseDto>>()
        typeIdItemsMap[deferType] = newIdItems
        newIdItems
      }
      idItemsMap.getOrElse(deferId) {
        val newItems = mutableSetOf<CddaItemParseDto>()
        idItemsMap[deferId] = newItems
        newItems
      }.add(cddaItemParseDto)
    }

    fun pop(type: CddaType, id: String): MutableSet<CddaItemParseDto> {
      val cddaItems = typeIdItemsMap.getOrDefault(type, mutableMapOf()).getOrDefault(id, mutableSetOf())
      val result = cddaItems.toMutableSet()
      cddaItems.clear()
      return result
    }
  }
}
