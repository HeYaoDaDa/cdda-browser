package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject.unit

import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.util.JsonUtil
import `fun`.hydd.cdda_browser.util.StringUtil

data class Time(@IgnoreMap var value: Long = 0) : CddaSubObject() {
  enum class Unit(val unit: Set<String>, val num: Long = 1) {
    MOVE(setOf("move")),
    S(setOf("turns", "turn", "second", "seconds", "s"), 100),
    M(setOf("minutes", "minute", "m"), 100 * 60),
    H(setOf("hours", "hour", "h"), 100 * 60 * 60),
    D(setOf("days", "day", "d"), 100 * 60 * 60 * 24)
  }

  override fun finalize(jsonValue: Any, param: String) {
    val defaultNum: Long = if (param.isBlank()) Unit.S.num else Unit.valueOf(param).num
    when (jsonValue) {
      is String -> {
        val valueUnit = StringUtil.splitValueUnit(jsonValue)
        if (valueUnit.second == null) {
          this.value = (valueUnit.first * defaultNum).toLong()
        } else {
          val unitNum = Unit.values().find { it.unit.contains(valueUnit.second) }?.num
            ?: throw Exception("unit ${valueUnit.second} miss")
          this.value = (valueUnit.first * unitNum).toLong()
        }
      }

      is Double -> this.value = (jsonValue * defaultNum).toLong()
      is Float -> this.value = (jsonValue * defaultNum).toLong()
      is Int -> this.value = jsonValue * defaultNum
      is Long -> this.value = jsonValue * defaultNum
      else -> throw IllegalArgumentException()
    }
  }

  override fun relative(relativeJson: Any, param: String) {
    val relative = Time()
    relative.finalize(relativeJson, param)
    this.value += relative.value
  }

  override fun proportional(proportionalJson: Any, param: String) {
    this.value = (this.value * JsonUtil.jsonToDouble(proportionalJson)).toLong()
  }
}
