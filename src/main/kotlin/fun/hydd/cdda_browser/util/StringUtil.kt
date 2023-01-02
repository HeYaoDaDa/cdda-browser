package `fun`.hydd.cdda_browser.util

import java.security.MessageDigest

object StringUtil {
  fun getStringHash(string: String): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val hash = messageDigest.digest(string.toByteArray())
    return hash.fold("") { str, it -> str + "%02x".format(it) }
  }

}
