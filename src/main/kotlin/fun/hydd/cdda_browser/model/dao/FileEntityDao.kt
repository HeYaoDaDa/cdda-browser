package `fun`.hydd.cdda_browser.model.dao

import `fun`.hydd.cdda_browser.model.entity.FileEntity
import `fun`.hydd.cdda_browser.util.extension.await
import org.hibernate.reactive.stage.Stage

object FileEntityDao {
  suspend fun findByHashCode(factory: Stage.SessionFactory, hashCode: String): FileEntity? {
    return factory.withSession {
      it.createQuery<FileEntity>("FROM FileEntity where hashCode = \'$hashCode\'").singleResultOrNull
    }.await()
  }
}
