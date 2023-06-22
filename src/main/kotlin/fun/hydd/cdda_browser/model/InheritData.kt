package `fun`.hydd.cdda_browser.model

import io.vertx.core.json.JsonObject

data class InheritData(
  val relative: JsonObject?,
  val proportional: JsonObject?,
  val extend: JsonObject?,
  val delete: JsonObject?,
)
