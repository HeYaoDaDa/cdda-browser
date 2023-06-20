package `fun`.hydd.cdda_browser.model.base

import `fun`.hydd.cdda_browser.model.CddaCommonItem
import `fun`.hydd.cdda_browser.model.CddaModDto
import `fun`.hydd.cdda_browser.model.FinalCddaItem
import `fun`.hydd.cdda_browser.model.bo.parse.CddaVersionDto
import `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.CddaItemRef
import org.slf4j.LoggerFactory

object ProcessContext {
  val finalManager = FinalManager
  val dependManager = DependManager
  val deferManager = DeferManager
  var version: CddaVersionDto? = null
  var mod: CddaModDto? = null
  var commonItem: CddaCommonItem? = null
  var itemId: String? = null

  private val log = LoggerFactory.getLogger(this.javaClass)

  object FinalManager {
    private val value: MutableMap<String, MutableMap<CddaItemRef, FinalCddaItem>> = mutableMapOf()

    fun popResult(): List<FinalCddaItem> {
      val result = value.flatMap { it.value.values }
      value.clear()
      return result
    }

    fun add(finalCddaItem: FinalCddaItem) {
      val cddaItemRef = CddaItemRef(finalCddaItem.cddaType, finalCddaItem.id)
      val modId = finalCddaItem.getMod().id
      val modOrderAndFinalCddaItemMap = value.getOrElse(modId) {
        val newMap = mutableMapOf<CddaItemRef, FinalCddaItem>()
        value[modId] = newMap
        newMap
      }
      modOrderAndFinalCddaItemMap[cddaItemRef] = finalCddaItem
    }

    fun find(mod: CddaModDto, cddaItemRef: CddaItemRef): List<FinalCddaItem> {
      val mods = mod.allDependencies.toMutableList()
      mods.add(mod)
      mods.reverse()
      return mods.mapNotNull { findBySingleModId(it.id, cddaItemRef) }
    }

    fun find(cddaItemRef: CddaItemRef): FinalCddaItem {
      return find(mod!!, cddaItemRef).firstOrNull() ?: throw NeedDeferException(cddaItemRef)
    }

    private fun findBySingleModId(modId: String, cddaItemRef: CddaItemRef): FinalCddaItem? {
      val modOrderAndFinalCddaItemMap = value[modId] ?: return null
      return modOrderAndFinalCddaItemMap[cddaItemRef]
    }
  }

  object DeferManager {
    private val value: MutableMap<CddaItemRef, MutableList<Pair<String, CddaCommonItem>>> = mutableMapOf()

    fun add(deferRef: CddaItemRef, commonItem: CddaCommonItem, id: String) {
      val deferItems = value.getOrElse(deferRef) {
        val newList = mutableListOf<Pair<String, CddaCommonItem>>()
        value[deferRef] = newList
        newList
      }
      deferItems.add(Pair(id, commonItem))
    }

    fun pop(ref: CddaItemRef): List<Pair<String, CddaCommonItem>> {
      val pairs = value[ref]
      return if (pairs == null) listOf() else {
        value.remove(ref)
        pairs
      }
    }

    fun check() {
      val filed = value.filter { it.value.isNotEmpty() }
      if (filed.isNotEmpty()) {
        filed.forEach { entry ->
          entry.value.forEach {
            log.warn("item ${it.second.cddaType}/${it.first} miss depend ${entry.key}")
          }
        }
        throw Exception("not clear defer map")
      }
    }
  }

  object DependManager {
    private val value: MutableMap<CddaItemRef, MutableSet<CddaItemRef>> = mutableMapOf()
    private val reversedValue: MutableMap<CddaItemRef, MutableSet<CddaItemRef>> = mutableMapOf()

    fun find(ref: CddaItemRef): Set<CddaItemRef> {
      return value[ref] ?: setOf()
    }

    fun findReversed(ref: CddaItemRef): Set<CddaItemRef> {
      return reversedValue[ref] ?: setOf()
    }

    fun add(baseRef: CddaItemRef, subRefs: Collection<CddaItemRef>) {
      subRefs.forEach { sub ->
        val subSet = value.getOrElse(baseRef) {
          val newSet = mutableSetOf<CddaItemRef>()
          value[baseRef] = newSet
          newSet
        }
        subSet.add(sub)
        val reversedSubSet = reversedValue.getOrElse(sub) {
          val newSet = mutableSetOf<CddaItemRef>()
          value[sub] = newSet
          newSet
        }
        reversedSubSet.add(baseRef)
      }
    }
  }
}
