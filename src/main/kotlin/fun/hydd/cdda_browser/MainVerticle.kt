package `fun`.hydd.cdda_browser

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import `fun`.hydd.cdda_browser.verticles.UpdateVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.core.json.jackson.DatabindCodec

class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    registerDataTypeModule()
    vertx.deployVerticle(UpdateVerticle()).onComplete {
      startPromise.complete()
    }.onFailure {
      it.printStackTrace()
    }.onComplete {
      vertx.close()
    }
  }

  /**
   * enable com.fasterxml.jackson.datatype:jackson-datatype-jsr310 to support Java 8 date/time
   */
  private fun registerDataTypeModule() {
    DatabindCodec.mapper().registerModule(JavaTimeModule())
    DatabindCodec.prettyMapper().registerModule(JavaTimeModule())
  }
}
