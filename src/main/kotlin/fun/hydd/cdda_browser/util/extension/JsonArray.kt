package `fun`.hydd.cdda_browser.util.extension

import `fun`.hydd.cdda_browser.util.JsonUtil
import `fun`.hydd.cdda_browser.util.StringUtil
import io.vertx.core.json.JsonArray

/**
 * get length 64 sha256 hash code
 * @receiver JsonArray
 * @return String's hash code, length 64
 */
fun JsonArray.getHashString(): String {
  return StringUtil.getStringHash(JsonUtil.sortJsonArray(this).toString())
}
