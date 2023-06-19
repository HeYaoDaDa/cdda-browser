package `fun`.hydd.cdda_browser.util.extension

import `fun`.hydd.cdda_browser.util.JsonUtil

fun String.Companion.parse(jsonValue: Any): String {
  return if (jsonValue is String) jsonValue
  else throw Exception("value is $jsonValue, type is ${jsonValue::class}")
}

fun Boolean.Companion.parse(jsonValue: Any): Boolean {
  return if (jsonValue is Boolean) jsonValue
  else throw Exception("value is $jsonValue, type is ${jsonValue::class}")
}

fun Double.Companion.parse(jsonValue: Any): Double {
  return when (jsonValue) {
    is Double -> jsonValue
    is Float -> jsonValue.toDouble()
    is Int -> jsonValue.toDouble()
    else -> throw IllegalArgumentException("value is $jsonValue, type is ${jsonValue::class}")
  }
}

fun Double.Companion.relative(relativeJson: Any, currentValue: Double): Double {
  return currentValue + Double.parse(relativeJson)
}

fun Double.Companion.proportional(proportionalJson: Any, currentValue: Double): Double {
  return JsonUtil.formatDouble(currentValue * Double.parse(proportionalJson))
}

fun Int.Companion.parse(jsonValue: Any): Int {
  return when (jsonValue) {
    is Double -> jsonValue.toInt()
    is Float -> jsonValue.toInt()
    is Int -> jsonValue
    else -> throw IllegalArgumentException("value is $jsonValue, type is ${jsonValue::class}")
  }
}

fun Int.Companion.relative(relativeJson: Any, currentValue: Int): Int {
  return currentValue + Int.parse(relativeJson)
}

fun Int.Companion.proportional(proportionalJson: Any, currentValue: Int): Int {
  return currentValue * Double.parse(proportionalJson).toInt()
}
