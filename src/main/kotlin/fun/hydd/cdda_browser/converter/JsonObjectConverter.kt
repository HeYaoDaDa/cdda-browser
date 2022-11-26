package `fun`.hydd.cdda_browser.converter

import io.vertx.core.json.JsonObject
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class JsonObjectConverter : AttributeConverter<JsonObject, String?> {
  override fun convertToDatabaseColumn(p0: JsonObject?): String? {
    return p0?.encode()
  }

  override fun convertToEntityAttribute(p0: String?): JsonObject? {
    return if (p0 == null) null else JsonObject(p0)
  }
}
