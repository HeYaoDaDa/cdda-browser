package `fun`.hydd.cdda_browser.verticles

import `fun`.hydd.cdda_browser.model.bo.restful.CddaRestfulVersion
import `fun`.hydd.cdda_browser.model.bo.restful.CddaWithItemRestfulMod
import `fun`.hydd.cdda_browser.model.dao.CddaModDao
import `fun`.hydd.cdda_browser.model.dao.CddaVersionDao
import `fun`.hydd.cdda_browser.model.dao.GetTextPoDao
import `fun`.hydd.cdda_browser.util.extension.coroutineRespond
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitBlocking
import org.hibernate.reactive.stage.Stage
import org.slf4j.LoggerFactory
import javax.persistence.Persistence

class RestfulVerticle : CoroutineVerticle() {
  private val log = LoggerFactory.getLogger(this.javaClass)
  private lateinit var factory: Stage.SessionFactory

  override suspend fun start() {
    super.start()

    factory = awaitBlocking {
      Persistence.createEntityManagerFactory("cdda-browser").unwrap(Stage.SessionFactory::class.java)
    }

    val server = vertx.createHttpServer()
    val mainRouter = Router.router(vertx)
    mainRouter.route().handler(this::handlerCheckCorsHeaders)

    mainRouter["/versions"].coroutineRespond { getAllVersion() }
    mainRouter["/mods/:versionId"].coroutineRespond {
      val versionId: Long = it.pathParam("versionId").toLong()
      getAllModByVersionId(versionId)
    }
    mainRouter["/po/:versionId/:languageCode"].coroutineRespond {
      val versionId: Long = it.pathParam("versionId").toLong()
      val languageCode: String = it.pathParam("languageCode")
      val byteArray = GetTextPoDao.getGetTextPoByVersionIdAndLanguageCode(
        factory,
        versionId,
        languageCode
      )?.fileEntity?.buffer?.toByteArray()
      Buffer.buffer(byteArray).toString("utf8")
    }

    server.requestHandler(mainRouter).listen(8081)
  }

  private suspend fun getAllVersion(): Collection<CddaRestfulVersion> {
    return CddaVersionDao.getAll(factory).map { it.toCddaRestfulVersion() }
  }

  private suspend fun getAllModByVersionId(versionId: Long): Collection<CddaWithItemRestfulMod> {
    val withItemsByVersionId = CddaModDao.getWithItemsByVersionId(factory, versionId)
    return withItemsByVersionId.map { it.toCddaWithItemRestfulMod() }
  }

  private fun handlerCheckCorsHeaders(ctx: RoutingContext) {
    val response = ctx.response()
    val origin = ctx.request().getHeader("Origin")
    if (origin != null) {
      response.putHeader("Access-Control-Allow-Origin", origin)
      // Tell browser that response might change with origin
      response.putHeader("Vary", "Origin")
      response.putHeader("Access-Control-Allow-Credentials", "true")
    }
    ctx.next()
  }
}
