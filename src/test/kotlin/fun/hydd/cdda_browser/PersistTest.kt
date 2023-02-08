package `fun`.hydd.cdda_browser

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.CddaVersionStatus
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.model.entity.CddaItem
import `fun`.hydd.cdda_browser.model.entity.CddaMod
import `fun`.hydd.cdda_browser.model.entity.CddaVersion
import io.vertx.core.Future
import io.vertx.core.Vertx
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
      cddaVersion.releaseName = "cataclysm Test Version"
      cddaVersion.tagName = "cataclysm-test-version"
      cddaVersion.experiment = true
      cddaVersion.status = CddaVersionStatus.STOP
      cddaVersion.releaseDate = LocalDateTime.now()
      cddaVersion.tagDate = LocalDateTime.now()
      cddaVersion.commitHash = "testHash"
      val cddaMod = CddaMod()
      cddaMod.modId = "test_mod"
      cddaMod.name = "test mod"
      cddaMod.description = "test mod description"
      cddaMod.obsolete = false
      cddaMod.core = false
      cddaMod.depModIds = mutableSetOf("dda")
      cddaMod.cddaVersion = cddaVersion
      cddaVersion.cddaMods.add(cddaMod)
      val cddaItem = CddaItem()
      cddaItem.jsonType = JsonType.MOD_INFO
      cddaItem.cddaType = CddaType.MOD_INFO
      cddaMod.cddaItems.add(cddaItem)
      val toCompletableFuture = emf.withTransaction { _, _ ->
        emf.withTransaction { session, _ -> session.persist(cddaVersion) }
      }.toCompletableFuture()
      Future.fromCompletionStage(toCompletableFuture, vertx.orCreateContext)
        .onComplete { testContext.completeNow() }
        .onFailure { testContext.failNow(it) }
    }
  }
}
