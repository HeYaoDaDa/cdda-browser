package `fun`.hydd.cdda_browser.server

import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.dto.CddaModDto
import `fun`.hydd.cdda_browser.util.getStringList
import io.vertx.core.json.JsonObject
import org.slf4j.LoggerFactory

class ModServer {
  private val log = LoggerFactory.getLogger(this.javaClass)

  fun parserCddaModJsonObject(jsonObject: JsonObject): CddaModDto {
    if (!JsonType.MOD_INFO.isEquals(jsonObject.getString("type"))) throw IllegalArgumentException("jsonObject type not is MOD_INFO")
    val cddaModDto = CddaModDto()
    cddaModDto.modId = jsonObject.getString("id")
    cddaModDto.name = jsonObject.getString("name")
    cddaModDto.description = jsonObject.getString("description")
    cddaModDto.obsolete = jsonObject.getBoolean("obsolete", false)
    cddaModDto.core = jsonObject.getBoolean("core", false)
    cddaModDto.depModIds = jsonObject.getStringList("dependencies", mutableListOf())!!.toHashSet()
    return cddaModDto
  }
}
