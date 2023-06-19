package `fun`.hydd.cdda_browser.model.bo.parse

import `fun`.hydd.cdda_browser.constant.CddaVersionStatus
import `fun`.hydd.cdda_browser.model.base.GitHubReleaseDto
import `fun`.hydd.cdda_browser.model.base.GitTagDto
import `fun`.hydd.cdda_browser.model.dao.CddaVersionDao
import `fun`.hydd.cdda_browser.util.GitUtil
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.hibernate.reactive.stage.Stage
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

data class CddaVersionDto(
  val releaseName: String,
  val tagName: String,
  val commitHash: String,
  val status: CddaVersionStatus,
  val experiment: Boolean,
  val tagDate: LocalDateTime,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun getPendUpdateVersions(vertx: Vertx, dbFactory: Stage.SessionFactory): List<CddaVersionDto> {
      val needUpdateVersions = mutableListOf<CddaVersionDto>()
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
      //todo test use
      return GitTagDto("0.G", LocalDateTime.now())
//      val tagRef = GitUtil.getLatestRevObject(eventBus)
//      return if (tagRef != null) GitTagDto(tagRef) else null
    }

    /**
     * Returns the CddaVersion list after the specified time (not include CddaVersion of specified time)
     *
     * @param date
     * @return
     */
    private suspend fun getNeedUpdateVersionList(vertx: Vertx, date: LocalDateTime): List<CddaVersionDto> =
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
    private suspend fun getCddaVersionByGitTagDto(vertx: Vertx, tag: GitTagDto): CddaVersionDto {
//      todo use in test
//      val requestOptions: RequestOptions =
//        RequestOptions().setHost("api.github.com").setURI("/repos/CleverRaven/Cataclysm-DDA/releases/tags/${tag.name}")
//          .setMethod(HttpMethod.GET).setPort(443).putHeader("User-Agent", "item-browser").setSsl(true)
//      val buffer = HttpUtil.request(vertx, requestOptions)
//      val releaseDto = if (buffer != null) {
//        val jsonObject: JsonObject = buffer.toJsonObject()
//        jsonObject.mapTo(GitHubReleaseDto::class.java)
//      } else {
//        throw Exception("Tag ${tag.name} release response is null")
//      }
      val releaseDto = GitHubReleaseDto("Gaiman", "0.G", "d6ec466140839dd70c1a43671eb4a08b007695c2", false, Date())
      if (tag.name != releaseDto.tagName) throw Exception("Tag and release not match")
      return CddaVersionDto(
        releaseDto.name, tag.name, releaseDto.commitHash, CddaVersionStatus.STOP, releaseDto.isExperiment, tag.date
      )
    }
  }
}
