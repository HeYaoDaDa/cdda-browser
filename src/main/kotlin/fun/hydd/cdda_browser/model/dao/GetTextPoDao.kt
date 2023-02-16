package `fun`.hydd.cdda_browser.model.dao

import `fun`.hydd.cdda_browser.model.entity.GetTextPo
import `fun`.hydd.cdda_browser.util.extension.await
import `fun`.hydd.cdda_browser.util.extension.fetch
import `fun`.hydd.cdda_browser.util.extension.get
import org.hibernate.reactive.stage.Stage

object GetTextPoDao {
  suspend fun getGetTextPoByVersionIdAndLanguageCode(
    factory: Stage.SessionFactory,
    versionId: Long,
    languageCode: String
  ): GetTextPo? {
    val builder = factory.criteriaBuilder
    val query = builder.createQuery(GetTextPo::class.java)
    val root = query.from(GetTextPo::class.java)
    query.distinct(true)
    query.select(root)
    query.where(
      builder.and(
        builder.equal(root.get(GetTextPo::language), languageCode),
        builder.equal(root.get(GetTextPo::version), versionId)
      )
    )
    root.fetch(GetTextPo::fileEntity)
    return factory.withSession { it.createQuery(query).singleResultOrNull }.await()
  }
}
