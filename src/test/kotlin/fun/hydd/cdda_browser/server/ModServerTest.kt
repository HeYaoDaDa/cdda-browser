package `fun`.hydd.cdda_browser.server

import `fun`.hydd.cdda_browser.dto.CddaModDto
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ModServerTest {
  private val modServer = ModServer()
  private var json1 = JsonObject()
  private var json2 = JsonObject()

  @BeforeEach
  fun initJson() {
    json1 = json {
      obj(
        "type" to "mod_info",
        "id" to "test_mod_id",
        "name" to "test mod",
        "description" to "test description",
      )
    }
    json2 = json {
      obj("type" to "WRONG")
    }
  }

  @Test
  fun parserCddaModJsonObject() {
    val cddaModDto = CddaModDto()
    cddaModDto.modId = "test_mod_id"
    cddaModDto.name = "test mod"
    cddaModDto.description = "test description"
    Assertions.assertEquals(cddaModDto, modServer.parserCddaModJsonObject(json1))
  }

  @Test
  fun parserCddaModJsonObjectWrongType() {
    Assertions.assertThrows(IllegalArgumentException::class.java) {
      modServer.parserCddaModJsonObject(json2)
    }
  }
}
