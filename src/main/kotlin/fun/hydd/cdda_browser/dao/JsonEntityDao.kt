package `fun`.hydd.cdda_browser.dao

import `fun`.hydd.cdda_browser.entity.JsonEntity
import `fun`.hydd.cdda_browser.extension.await
import org.hibernate.reactive.stage.Stage

object JsonEntityDao {


  suspend fun findByHashCode(factory: Stage.SessionFactory, hashCode: String): JsonEntity? = factory.withSession {
    it.createQuery<JsonEntity>("FROM JsonEntity where hashCode = \'$hashCode\'").singleResultOrNull
  }.await()

  suspend fun first(factory: Stage.SessionFactory): JsonEntity? = factory.withSession {
    it.createQuery<JsonEntity>("FROM JsonEntity").setMaxResults(1).singleResultOrNull
  }.await()

}
