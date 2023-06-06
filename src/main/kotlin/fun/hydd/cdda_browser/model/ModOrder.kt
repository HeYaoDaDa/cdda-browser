package `fun`.hydd.cdda_browser.model

data class ModOrder(
  val value: List<CddaModDto>
) {
  fun contains(modOrder: ModOrder): Boolean {
    if (modOrder.value.isEmpty()) return false
    if (this.value == modOrder.value) return true
    if (this.value.size < modOrder.value.size) return false
    var max = 0
    modOrder.value.forEachIndexed { index, cddaMod ->
      val indexOf = this.value.indexOf(cddaMod)
      if (indexOf < 0) return false
      else if (indexOf < index) return false
      else if (indexOf < max) return false
      max = indexOf
    }
    return true
  }

  override fun toString(): String {
    return "[${value.map { it.id }.joinToString(", ")}]"
  }

  companion object {
    fun getAllMissModOrder(modOrders: Set<ModOrder>): List<ModOrder> {
      val mods: MutableSet<CddaModDto> = mutableSetOf()
      modOrders.forEach { mods.addAll(it.value) }
      val allModOrder = getAllModOrder(mods)
      return allModOrder.filter { !modOrders.contains(it) }
    }

    private fun getAllModOrder(mods: Set<CddaModDto>): List<ModOrder> {
      val allMods: MutableList<List<CddaModDto>> = mutableListOf()
      getAllModList(mods, allMods)
      return allMods.filter { isApproval(it) }.map { ModOrder(it) }
    }

    private fun getAllModList(
      pendSet: Set<CddaModDto>,
      result: MutableList<List<CddaModDto>>,
      currentList: List<CddaModDto> = listOf()
    ) {
      if (currentList.isNotEmpty()) result.add(currentList)
      pendSet.forEach {
        val temp = pendSet.toMutableSet()
        temp.remove(it)
        val newList = currentList.toMutableList()
        newList.add(it)
        getAllModList(temp, result, currentList.toMutableList())
      }
    }

    private fun isApproval(mods: List<CddaModDto>): Boolean {
      for ((index, mod) in mods.withIndex()) {
        if (index == 0) {
          if (mod.allDependencies.isNotEmpty()) return false
        } else {
          if (!mods.subList(0, index).containsAll(mod.allDependencies)) return false
        }
      }
      return true
    }
  }
}
