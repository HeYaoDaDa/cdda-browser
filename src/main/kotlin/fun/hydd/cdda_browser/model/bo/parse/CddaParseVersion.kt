package `fun`.hydd.cdda_browser.model.bo.parse

import `fun`.hydd.cdda_browser.constant.CddaVersionStatus
import `fun`.hydd.cdda_browser.model.base.GitHubReleaseDto
import `fun`.hydd.cdda_browser.model.base.GitTagDto
import `fun`.hydd.cdda_browser.model.dao.CddaVersionDao
import `fun`.hydd.cdda_browser.util.GitUtil
import `fun`.hydd.cdda_browser.util.HttpUtil
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.hibernate.reactive.stage.Stage
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class CddaParseVersion {
  var id: Long? = null

  lateinit var releaseName: String

  lateinit var tagName: String

  lateinit var commitHash: String

  lateinit var status: CddaVersionStatus

  var experiment: Boolean = true

  lateinit var tagDate: LocalDateTime

  val mods: MutableCollection<CddaParseMod> = mutableListOf()

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun getPendUpdateVersions(vertx: Vertx, dbFactory: Stage.SessionFactory): List<CddaParseVersion> {
      val needUpdateVersions = mutableListOf<CddaParseVersion>()
      val repoLatestTag = getRepoLatestVersionTag(vertx.eventBus())
      if (repoLatestTag != null) {
        val savedLatestVersionDto = CddaVersionDao.getLatest(dbFactory)
        if (savedLatestVersionDto == null) {
          log.warn("No saved version, only update latest once version")
          needUpdateVersions.add(getCddaVersionByGitTagDto(vertx, repoLatestTag))
        } else {
          val dbLatestVersionDate = savedLatestVersionDto.tagDate!!
          val repoLatestTagDate = repoLatestTag.date
          if (dbLatestVersionDate.isAfter(repoLatestTagDate)) {
            throw Exception("db version after repo version")
          } else if (dbLatestVersionDate.isBefore(repoLatestTagDate)) {
            needUpdateVersions.addAll(getNeedUpdateVersionList(vertx, dbLatestVersionDate))
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
    private suspend fun getRepoLatestVersionTag(eventBus: EventBus): GitTagDto? {
      val tagRef = GitUtil.getLatestRevObject(eventBus)
      return if (tagRef != null) GitTagDto(tagRef) else null
    }

    /**
     * Returns the CddaVersion list after the specified time (not include CddaVersion of specified time)
     *
     * @param date
     * @return
     */
    private suspend fun getNeedUpdateVersionList(vertx: Vertx, date: LocalDateTime): List<CddaParseVersion> =
      coroutineScope {
        val localRefs = GitUtil.getTagList(vertx.eventBus())
        val result = ArrayList<GitTagDto>()
        for (localRef in localRefs) {
          val gitTagDto = GitTagDto(GitUtil.getRevObject(vertx.eventBus(), localRef))
          if (gitTagDto.date.isAfter(date)) {
            result.add(gitTagDto)
          }
        }
        val afterTagList = result.sortedBy { it.date }
        afterTagList.map { async { getCddaVersionByGitTagDto(vertx, it) } }.awaitAll()
      }

    /**
     * get CddaVersion by GitTagDto
     *
     * @param tag GitTagDto
     * @return CddaVersion
     */
    private suspend fun getCddaVersionByGitTagDto(vertx: Vertx, tag: GitTagDto): CddaParseVersion {
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
}
