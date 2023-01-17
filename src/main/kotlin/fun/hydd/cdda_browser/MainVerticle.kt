package `fun`.hydd.cdda_browser

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import `fun`.hydd.cdda_browser.model.codec.UnitCodec
import `fun`.hydd.cdda_browser.verticles.GitRepoVerticle
import `fun`.hydd.cdda_browser.verticles.UpdateVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import java.util.concurrent.TimeUnit

class MainVerticle : CoroutineVerticle() {

  override suspend fun start() {
    registerDataTypeModule()
    vertx.eventBus().registerDefaultCodec(Unit.javaClass, UnitCodec())
    vertx.deployVerticle(
      GitRepoVerticle(),
      DeploymentOptions()
        .setWorker(true)
        .setWorkerPoolName("git-repo-pool")
        .setMaxWorkerExecuteTime(80)
        .setMaxWorkerExecuteTimeUnit(TimeUnit.SECONDS)
    )
      .await()
    vertx.deployVerticle(UpdateVerticle()).await()
  }

  /**
   * enable com.fasterxml.jackson.datatype:jackson-datatype-jsr310 to support Java 8 date/time
   */
  private fun registerDataTypeModule() {
    DatabindCodec.mapper().registerModule(JavaTimeModule())
    DatabindCodec.prettyMapper().registerModule(JavaTimeModule())
  }
}
