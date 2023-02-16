package `fun`.hydd.cdda_browser.verticles

import `fun`.hydd.cdda_browser.constant.CddaVersionStatus
import `fun`.hydd.cdda_browser.model.base.GitHubReleaseDto
import `fun`.hydd.cdda_browser.model.base.GitTagDto
import `fun`.hydd.cdda_browser.model.bo.parse.CddaParseVersion
import `fun`.hydd.cdda_browser.model.dao.CddaVersionDao
import `fun`.hydd.cdda_browser.model.dao.JsonEntityDao
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    log.info("UpdateVerticle start")
    super.start()
    init()
    vertx.setTimer(1_000) { launch { update() } }
    log.info("UpdateVerticle end")
  }

  private suspend fun update() {
    log.info("update start")
    GitUtil.update(vertx.eventBus())
    val needUpdateVersions = getNeedUpdateVersions()
    log.info("need update version size is ${needUpdateVersions.size}")
    for (parseVersion in needUpdateVersions) {
      log.info("start update version ${parseVersion.tagName}")
      GitUtil.hardRestToTag(vertx.eventBus(), parseVersion.tagName)
      parseVersion.mods.addAll(ModServer.getCddaModDtoList(vertx.fileSystem(), repoDir.absolutePath))
      CddaItemParseManager.parseCddaVersion(parseVersion)
      val cddaVersion = parseVersion.toCddaVersion(factory)
      cddaVersion.pos =
        GetTextPoServer.getTextPosByRepo(vertx.fileSystem(), factory, repoDir.absolutePath, cddaVersion)
      CddaVersionDao.save(factory, cddaVersion)
      log.info("end update version ${parseVersion.tagName}")
      log.info("\n" + JsonEntityDao.first(factory)!!.json!!.encodePrettily())
    }
    log.info("update end")
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

  private suspend fun getNeedUpdateVersions(): List<CddaParseVersion> {
    val needUpdateVersions = mutableListOf<CddaParseVersion>()
    val repoLatestTag = getRepoLatestVersionTag()
    if (repoLatestTag != null) {
      val savedLatestVersionDto = CddaVersionDao.getLatest(factory)
      if (savedLatestVersionDto == null) {
        log.warn("No saved version, only update latest once version")
        needUpdateVersions.add(getCddaVersionByGitTagDto(repoLatestTag))
      } else {
        val dbLatestVersionDate = savedLatestVersionDto.tagDate!!
        val repoLatestTagDate = repoLatestTag.date
        if (dbLatestVersionDate.isAfter(repoLatestTagDate)) {
          throw Exception("db version after repo version")
        } else if (dbLatestVersionDate.isBefore(repoLatestTagDate)) {
          needUpdateVersions.addAll(getNeedUpdateVersionList(dbLatestVersionDate))
        }
      }
    } else {
      throw Exception("No find repo tag!")
    }
    return needUpdateVersions
  }

  /**
   * Return repo the latest tag
   *
   * @return tag
   */
  private suspend fun getRepoLatestVersionTag(): GitTagDto? {
    val tagRef = GitUtil.getLatestRevObject(vertx.eventBus())
    return if (tagRef != null) GitTagDto(tagRef) else null
  }

  /**
   * Returns the CddaVersion list after the specified time (not include CddaVersion of specified time)
   *
   * @param date
   * @return
   */
  private suspend fun getNeedUpdateVersionList(date: LocalDateTime): List<CddaParseVersion> {
    val localRefs = GitUtil.getTagList(vertx.eventBus())
    val result = ArrayList<GitTagDto>()
    for (localRef in localRefs) {
      val gitTagDto = GitTagDto(GitUtil.getRevObject(vertx.eventBus(), localRef))
      if (gitTagDto.date.isAfter(date)) {
        result.add(gitTagDto)
      }
    }
    val afterTagList = result.sortedBy { it.date }
    return afterTagList.map { async { getCddaVersionByGitTagDto(it) } }.awaitAll()
  }

  /**
   * get CddaVersion by GitTagDto
   *
   * @param tag GitTagDto
   * @return CddaVersion
   */
  private suspend fun getCddaVersionByGitTagDto(tag: GitTagDto): CddaParseVersion {
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
    if (tag.name != releaseDto.tagName) throw Exception("Tag and release not match")
    val result = CddaParseVersion()
    result.releaseName = releaseDto.name
    result.commitHash = releaseDto.commitHash
    result.experiment = releaseDto.isExperiment
    result.tagName = tag.name
    result.tagDate = tag.date
    result.status = CddaVersionStatus.STOP
    return result
  }

}
