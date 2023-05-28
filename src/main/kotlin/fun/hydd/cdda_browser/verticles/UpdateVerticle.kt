package `fun`.hydd.cdda_browser.verticles

import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseMod
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseVersion
import `fun`.hydd.cdda_browser.model.dao.CddaVersionDao
import `fun`.hydd.cdda_browser.model.dao.JsonEntityDao
import `fun`.hydd.cdda_browser.model.entity.CddaVersion
import `fun`.hydd.cdda_browser.server.CddaItemParseManager
import `fun`.hydd.cdda_browser.util.GitUtil
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitBlocking
import kotlinx.coroutines.launch
import org.hibernate.reactive.stage.Stage
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
import javax.persistence.Persistence


class UpdateVerticle : CoroutineVerticle() {
  private val log = LoggerFactory.getLogger(this.javaClass)

  private lateinit var dbFactory: Stage.SessionFactory
  private lateinit var repoDir: File

  override suspend fun start() {
    log.info("UpdateVerticle deploy start")
    super.start()
    init()
    vertx.setTimer(1_000) { launch { update() } }
    log.info("UpdateVerticle deploy end")
  }

  private suspend fun update() {
    log.info("update start")
    GitUtil.update(vertx.eventBus())
    val pendUpdateVersions = CddaParseVersion.getPendUpdateVersions(vertx, dbFactory)
    log.info(
      "pend update version size is ${pendUpdateVersions.size}" +
        "\n\t${pendUpdateVersions.joinToString("\n\t") { it.tagName }}"
    )
    for (pendUpdateVersion in pendUpdateVersions) {
      log.info("update version ${pendUpdateVersion.tagName} start")
      GitUtil.hardRestToTag(vertx.eventBus(), pendUpdateVersion.tagName)
      val cddaModDtoList = CddaParseMod.getCddaModDtoList(vertx.fileSystem(), repoDir.absolutePath)
      log.info(
        "mod size is ${cddaModDtoList.size}" +
          "\n\t${cddaModDtoList.joinToString("\n\t") { it.id }}"
      )
      pendUpdateVersion.mods.addAll(cddaModDtoList)
      CddaItemParseManager.parseCddaVersion(pendUpdateVersion)
      val cddaVersion = CddaVersion.of(dbFactory, pendUpdateVersion)
//      cddaVersion.pos =
//        GetTextPoServer.getTextPosByRepo(vertx.fileSystem(), dbFactory, repoDir.absolutePath, cddaVersion)
      CddaVersionDao.save(dbFactory, cddaVersion)
      log.info("update version ${pendUpdateVersion.tagName} end")
      log.info("\n" + JsonEntityDao.first(dbFactory)!!.json!!.encodePrettily())
    }
    log.info("update end")
    // todo only use in test
    vertx.close()
  }

  /**
   * suspend init verticle
   *
   */
  private suspend fun init() {
    log.info("init start")
    repoDir = Paths.get(config.getString("user.home"), config.getJsonObject("repository").getString("path")).toFile()
    dbFactory = awaitBlocking {
      Persistence.createEntityManagerFactory("cdda-browser").unwrap(Stage.SessionFactory::class.java)
    }
    log.info("init end")
  }
}
