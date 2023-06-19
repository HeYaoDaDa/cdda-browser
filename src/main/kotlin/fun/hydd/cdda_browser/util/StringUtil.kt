package `fun`.hydd.cdda_browser.util

import java.security.MessageDigest

object StringUtil {
  fun getStringHash(string: String): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val hash = messageDigest.digest(string.toByteArray())
    return hash.fold("") { str, it -> str + "%02x".format(it) }
  }

  fun splitValueUnit(source: String): Pair<Double, String?> {
    val pattern = Regex("([0-9]+)\\s*([A-Za-z]+)?")
    val match = pattern.find(source)
    val value = match?.groups?.get(1)?.value?.toDouble() ?: throw Exception("source $source miss value")
    val unit = match.groups[2]?.value?.lowercase()
    return Pair(value, unit)
  }
}
