package `fun`.hydd.cdda_browser.model.dao

import `fun`.hydd.cdda_browser.model.entity.CddaVersion
import `fun`.hydd.cdda_browser.util.extension.await
import org.hibernate.reactive.stage.Stage.SessionFactory

object CddaVersionDao {
  suspend fun save(factory: SessionFactory, version: CddaVersion) {
    factory.withTransaction { session, _ -> session.persist(version) }.await()
  }

  suspend fun getLatest(factory: SessionFactory): CddaVersion? = factory.withSession {
    it.createQuery<CddaVersion>("FROM CddaVersion ORDER BY releaseDate DESC").setMaxResults(1).singleResultOrNull
  }.await()
}
