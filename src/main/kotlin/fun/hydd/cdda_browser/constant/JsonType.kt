package `fun`.hydd.cdda_browser.constant

enum class JsonType(private val str: String) {
  MOD_INFO("mod_info"),
  JSON_FLAG("json_flag"),
  BODY_PART("body_part"),
  SUB_BODY_PART("sub_body_part"),
  MATERIAL("material");

  fun isEquals(other: Any?): Boolean {
    if (this === other) return true
    if (String::class.java != other?.javaClass) return false

    other as String

    return this.str.lowercase() == other.lowercase()
  }
}
