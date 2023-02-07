package `fun`.hydd.cdda_browser

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import `fun`.hydd.cdda_browser.model.codec.UnitCodec
import `fun`.hydd.cdda_browser.verticles.GitRepoVerticle
import `fun`.hydd.cdda_browser.verticles.UpdateVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.jackson.DatabindCodec
import java.util.concurrent.TimeUnit

fun main() {
  val vertx = Vertx.vertx()
  DatabindCodec.mapper().registerModule(JavaTimeModule())
  DatabindCodec.prettyMapper().registerModule(JavaTimeModule())

  vertx.eventBus().registerDefaultCodec(Unit.javaClass, UnitCodec())

  vertx.deployVerticle(
    GitRepoVerticle(),
    DeploymentOptions()
      .setWorker(true)
      .setWorkerPoolName("git-repo-pool")
      .setMaxWorkerExecuteTime(80)
      .setMaxWorkerExecuteTimeUnit(TimeUnit.SECONDS)
  ).compose {
    vertx.deployVerticle(UpdateVerticle())
  }
}

