package `fun`.hydd.cdda_browser.verticles

import `fun`.hydd.cdda_browser.dao.CddaVersionDao
import `fun`.hydd.cdda_browser.dao.JsonEntityDao
import `fun`.hydd.cdda_browser.entity.CddaVersion
import `fun`.hydd.cdda_browser.model.base.GitHubReleaseDto
import `fun`.hydd.cdda_browser.model.base.GitTagDto
import `fun`.hydd.cdda_browser.server.CddaItemParseManager
import `fun`.hydd.cdda_browser.server.GetTextPoServer
import `fun`.hydd.cdda_browser.server.ModServer
import `fun`.hydd.cdda_browser.util.GitUtil
import `fun`.hydd.cdda_browser.util.HttpUtil
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitBlocking
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.hibernate.reactive.stage.Stage
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
import java.time.LocalDateTime
import javax.persistence.Persistence


class UpdateVerticle : CoroutineVerticle() {
  private val log = LoggerFactory.getLogger(this.javaClass)
  private lateinit var factory: Stage.SessionFactory
  private val repoDir: File = Paths.get(System.getProperty("user.home"), "Documents", "Cataclysm-DDA").toFile()

  override suspend fun start() {
    super.start()
    init()
    vertx.setTimer(1_000) { GlobalScope.launch(vertx.dispatcher()) { update() } }
  }

  private suspend fun update() {
    GitUtil.update(vertx.eventBus())
    val updateVersionList = getNeedUpdateVersions()
    log.info("Need update version size is ${updateVersionList.size}")
    for (cddaVersion in updateVersionList) {
      GitUtil.hardRestToTag(vertx.eventBus(), cddaVersion.tagName!!)
      val cddaModDtoList = ModServer.getCddaModDtoList(vertx.fileSystem(), repoDir.absolutePath)
      val cddaItemParseManager = CddaItemParseManager(cddaVersion, cddaModDtoList)
      cddaItemParseManager.parseAll(factory)
      cddaVersion.pos = GetTextPoServer.getTextPosByRepo(vertx.fileSystem(), factory, repoDir.absolutePath, cddaVersion)
      CddaVersionDao.save(factory, cddaVersion)
      log.info("\n" + JsonEntityDao.first(factory)!!.json!!.encodePrettily())
    }
    vertx.close()
  }


  /**
   * suspend init verticle
   *
   */
  private suspend fun init() {
    log.info("Start init")
    factory = awaitBlocking {
      Persistence.createEntityManagerFactory("cdda-browser").unwrap(Stage.SessionFactory::class.java)
    }
    log.info("End init")
  }

  private suspend fun getNeedUpdateVersions(): List<CddaVersion> {
    val updateVersionList = mutableListOf<CddaVersion>()
    val repoLatestVersionDto = getRepoLatestVersionDto()
    if (repoLatestVersionDto != null) {
      val dbLatestVersionDto = CddaVersionDao.getLatest(factory)
      if (dbLatestVersionDto == null) {
        updateVersionList.add(repoLatestVersionDto)
      } else {
        val dbLatestVersionDate = dbLatestVersionDto.tagDate
        val repoLatestVersionDate = repoLatestVersionDto.tagDate
        if (dbLatestVersionDate!!.isAfter(repoLatestVersionDate)) {
          throw Exception("db version after repo version")
        } else if (dbLatestVersionDate.isBefore(repoLatestVersionDate)) {
          updateVersionList.addAll(getNeedUpdateVersionList(dbLatestVersionDate))
        }
      }
    } else {
      log.warn("No find repo tag!")
    }
    return updateVersionList
  }

  /**
   * Return latest CddaVersionDto for current repo status
   */
  private suspend fun getRepoLatestVersionDto(): CddaVersion? {
    val tagRef = GitUtil.getLatestRevObject(vertx.eventBus())
    val gitTagDto = if (tagRef != null) GitTagDto(tagRef) else null
    return if (gitTagDto != null) getCddaVersionByGitTagDto(gitTagDto) else null
  }

  /**
   * Returns the CddaVersion list after the specified time (not include CddaVersion of specified time)
   *
   * @param date
   * @return
   */
  private suspend fun getNeedUpdateVersionList(date: LocalDateTime): List<CddaVersion> {
    val localRefs = GitUtil.getTagList(vertx.eventBus())
    val result = ArrayList<GitTagDto>()
    for (localRef in localRefs) {
      val gitTagDto = GitTagDto(GitUtil.getRevObject(vertx.eventBus(), localRef))
      if (gitTagDto.date.isAfter(date)) {
        result.add(gitTagDto)
      }
    }
    val afterTagList = result.sortedBy { it.date }
    return afterTagList.map { getCddaVersionByGitTagDto(it) }
  }

  /**
   * get CddaVersion by GitTagDto
   *
   * @param tag GitTagDto
   * @return CddaVersion
   */
  private suspend fun getCddaVersionByGitTagDto(tag: GitTagDto): CddaVersion {
    val requestOptions: RequestOptions =
      RequestOptions().setHost("api.github.com").setURI("/repos/CleverRaven/Cataclysm-DDA/releases/tags/${tag.name}")
        .setMethod(HttpMethod.GET).setPort(443).putHeader("User-Agent", "item-browser").setSsl(true)
    val buffer = HttpUtil.request(vertx, requestOptions)
    val releaseDto = if (buffer != null) {
      val jsonObject: JsonObject = buffer.toJsonObject()
      jsonObject.mapTo(GitHubReleaseDto::class.java)
    } else {
      throw Exception("Tag ${tag.name} release response is null")
    }
    return CddaVersion.of(tag, releaseDto)
  }

}
