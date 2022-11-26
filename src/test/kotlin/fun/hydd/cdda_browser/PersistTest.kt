package `fun`.hydd.cdda_browser

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.CddaVersionStatus
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.entity.CddaMod
import `fun`.hydd.cdda_browser.entity.CddaObject
import `fun`.hydd.cdda_browser.entity.CddaVersion
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.hibernate.reactive.stage.Stage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import javax.persistence.Persistence

@ExtendWith(VertxExtension::class)
class PersistTest {

  @Test
  fun saveData(vertx: Vertx, testContext: VertxTestContext) {
    vertx.executeBlocking {
      it.complete(Persistence.createEntityManagerFactory("cdda-browser").unwrap(Stage.SessionFactory::class.java))
    }.compose { emf ->
      val cddaVersion = CddaVersion()
      cddaVersion.name = "cataclysm Test Version"
      cddaVersion.tagName = "cataclysm-test-version"
      cddaVersion.experiment = true
      cddaVersion.status = CddaVersionStatus.STOP
      cddaVersion.publishedAt = LocalDateTime.now()
      val cddaMod = CddaMod()
      cddaMod.name = "test mod"
      cddaMod.obsolete = false
      cddaMod.core = false
      cddaMod.cddaVersion = cddaVersion
      cddaVersion.cddaMods.add(cddaMod)
      val cddaObject = CddaObject()
      cddaObject.jsonType = JsonType.MOD_INFO
      cddaObject.cddaType = CddaType.MOD_INFO
      cddaObject.jsonObject = JsonObject.of()
      cddaMod.cddaObjects.add(cddaObject)
      val toCompletableFuture = emf.withTransaction { _, _ ->
        emf.withTransaction { session, _ -> session.persist(cddaVersion) }
      }.toCompletableFuture()
      Future.fromCompletionStage(toCompletableFuture, vertx.orCreateContext)
        .onComplete { testContext.completeNow() }
        .onFailure { testContext.failNow(it) }
    }
  }
}
