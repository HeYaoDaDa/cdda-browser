package `fun`.hydd.cdda_browser.model.dao

import `fun`.hydd.cdda_browser.model.entity.CddaItem
import `fun`.hydd.cdda_browser.model.entity.CddaMod
import `fun`.hydd.cdda_browser.util.extension.await
import `fun`.hydd.cdda_browser.util.extension.fetch
import `fun`.hydd.cdda_browser.util.extension.fetchCollection
import `fun`.hydd.cdda_browser.util.extension.get
import org.hibernate.reactive.stage.Stage

object CddaModDao {

  //TODO change to NamedEntityGraph
  suspend fun getWithItemsByVersionId(factory: Stage.SessionFactory, versionId: Long): Collection<CddaMod> {
    val builder = factory.criteriaBuilder
    val query = builder.createQuery(CddaMod::class.java)
    val root = query.from(CddaMod::class.java)
    query.distinct(true)
    query.select(root)
    query.where(builder.equal(root.get(CddaMod::version), versionId))
    val fetchCddaItem = root.fetchCollection(CddaMod::items)
    fetchCddaItem.fetch(CddaItem::originalJson)
    fetchCddaItem.fetch(CddaItem::json)
    return factory.withSession { it.createQuery(query).resultList }.await()
  }
}
