package `fun`.hydd.cdda_browser.constant

enum class JsonType(private val str: String) {
  MOD_INFO("mod_info"),
  JSON_FLAG("json_flag"),
  WEAPON_CATEGORY("weapon_category"),
  ASCII_ART("ascii_art"),
  SKILL_DISPLAY_TYPE("skill_display_type"),
  SKILL("skill"),
  MARTIAL_ART("martial_art"),
  BODY_PART("body_part"),
  SUB_BODY_PART("sub_body_part"),
  MATERIAL("material"),
  ITEM_CATEGORY("item_category"),
  AMMUNITION_TYPE("ammunition_type"),

  GENERIC("generic"),
  BIONIC_ITEM("bionic_item"),
  BATTERY("battery"),
  MAGAZINE("magazine"),
  GUNMOD("gunmod"),
  WHEEL("wheel"),
  ENGINE("engine"),
  COMESTIBLE("comestible"),
  BOOK("book"),
  TOOL_ARMOR("tool_armor"),
  TOOLMOD("toolmod"),
  TOOL("tool"),
  PET_ARMOR("pet_armor"),
  ARMOR("armor"),
  GUN("gun"),
  AMMO("ammo");


  fun isEquals(other: Any?): Boolean {
    if (this === other) return true
    if (String::class.java != other?.javaClass) return false

    other as String

    return this.str.lowercase() == other.lowercase()
  }
}
