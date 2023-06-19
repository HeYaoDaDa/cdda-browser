package `fun`.hydd.cdda_browser.verticles

import `fun`.hydd.cdda_browser.model.CddaModDto
import `fun`.hydd.cdda_browser.model.CddaModJsonEntity
import `fun`.hydd.cdda_browser.model.base.ProcessContext
import `fun`.hydd.cdda_browser.model.bo.parse.CddaVersionDto
import `fun`.hydd.cdda_browser.model.dao.CddaVersionDao
import `fun`.hydd.cdda_browser.model.entity.CddaItem
import `fun`.hydd.cdda_browser.model.entity.CddaMod
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
    log.info("update data start")
    GitUtil.update(vertx.eventBus())
    val pendUpdateVersions = CddaVersionDto.getPendUpdateVersions(vertx, dbFactory)
    log.info(
      "pend update version size is ${pendUpdateVersions.size}" +
        "\n\t${pendUpdateVersions.joinToString("\n\t") { it.tagName }}"
    )
    for (pendUpdateVersion in pendUpdateVersions) {
      log.info("---version ${pendUpdateVersion.tagName} start---")
      ProcessContext.version = pendUpdateVersion
      GitUtil.hardRestToTag(vertx.eventBus(), pendUpdateVersion.tagName)
      val mods = CddaModDto.ofList(CddaModJsonEntity.getCddaModJsonEntityList(vertx.fileSystem(), repoDir.absolutePath))
      log.info(
        "---mod size is ${mods.size}---\n" +
          "---[${mods.joinToString(", ") { it.id }}]---"
      )
      val finalCddaItems = CddaItemParseManager.process(vertx.fileSystem(), mods)
      val cddaVersion = CddaVersion.of(pendUpdateVersion)
      val cddaMods = mods.map { CddaMod.of(it) }
      cddaMods.forEach { it.version = cddaVersion }
      cddaVersion.mods.addAll(cddaMods)
      CddaItem.ofList(dbFactory, repoDir.toPath(), finalCddaItems, cddaMods)
//      val pos = GetTextPoServer.getTextPosByRepo(vertx.fileSystem(), dbFactory, repoDir.absolutePath)
//      pos.forEach { it.version = cddaVersion }
//      cddaVersion.pos.addAll(pos)
      CddaVersionDao.save(dbFactory, cddaVersion)
      ProcessContext.version = null
      log.info("---version ${pendUpdateVersion.tagName} end---")
    }
    log.info("update data end")
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
