package `fun`.hydd.cdda_browser.constant

enum class JsonType(private val str: String) {
  MOD_INFO("mod_info"),
  JSON_FLAG("json_flag");

  fun isEquals(other: Any?): Boolean {
    if (this === other) return true
    if (String::class.java != other?.javaClass) return false

    other as String

    return this.str.lowercase() == other.lowercase()
  }
}
