package `fun`.hydd.cdda_browser.constant

enum class JsonType(val str: String) {
  MOD_INFO("MOD_INFO");

  fun isEquals(other: Any?): Boolean {
    if (this === other) return true
    if (String::class.java != other?.javaClass) return false

    other as String

    return this.str.lowercase() == other.lowercase()
  }
}
