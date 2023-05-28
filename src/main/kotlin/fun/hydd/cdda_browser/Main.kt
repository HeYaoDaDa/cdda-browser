package `fun`.hydd.cdda_browser

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import `fun`.hydd.cdda_browser.model.codec.UnitCodec
import `fun`.hydd.cdda_browser.verticles.GitRepoVerticle
import `fun`.hydd.cdda_browser.verticles.RestfulVerticle
import `fun`.hydd.cdda_browser.verticles.UpdateVerticle
import io.vertx.config.ConfigRetriever
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import java.util.concurrent.TimeUnit

fun main() {
  bindJavaTimeModule()
  val vertx = Vertx.vertx()
  registerEventBusCodec(vertx)
  ConfigRetriever.create(vertx)
    .config.onSuccess { config ->
      vertx.deployVerticle(RestfulVerticle())
      vertx.deployVerticle(
        GitRepoVerticle(),
        DeploymentOptions()
          .setConfig(config)
          .setWorker(true)
          .setWorkerPoolName("git-repo-pool")
          .setMaxWorkerExecuteTime(20)
          .setMaxWorkerExecuteTimeUnit(TimeUnit.MINUTES)
      ).compose {
        vertx.deployVerticle(UpdateVerticle(), DeploymentOptions().setConfig(config))
      }
    }
}

private fun bindJavaTimeModule() {
  val javaTimeModule = JavaTimeModule()
  DatabindCodec.mapper().registerModule(javaTimeModule)
  DatabindCodec.prettyMapper().registerModule(javaTimeModule)
}

private fun registerEventBusCodec(vertx: Vertx) {
  vertx.eventBus().registerDefaultCodec(Unit.javaClass, UnitCodec())
}

