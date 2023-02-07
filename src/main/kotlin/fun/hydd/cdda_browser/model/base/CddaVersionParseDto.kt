package `fun`.hydd.cdda_browser.model.base

import com.googlecode.jmapper.JMapper
import com.googlecode.jmapper.annotations.JGlobalMap
import com.googlecode.jmapper.enums.ChooseConfig
import `fun`.hydd.cdda_browser.constant.CddaVersionStatus
import `fun`.hydd.cdda_browser.entity.CddaVersion
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.hibernate.reactive.stage.Stage
import java.time.LocalDateTime
import java.time.ZoneOffset

@JGlobalMap(classes = [CddaVersion::class], excluded = ["cddaMods", "Companion"])
class CddaVersionParseDto() {
  var id: Long? = null

  lateinit var releaseName: String

  lateinit var tagName: String

  lateinit var commitHash: String

  lateinit var status: CddaVersionStatus

  var experiment: Boolean = true

  lateinit var releaseDate: LocalDateTime

  lateinit var tagDate: LocalDateTime

  val cddaMods: MutableCollection<CddaModParseDto> = mutableListOf()

  suspend fun toEntity(factory: Stage.SessionFactory): CddaVersion {
    val jMapper = JMapper(CddaVersion::class.java, CddaVersionParseDto::class.java, ChooseConfig.SOURCE)
    val cddaVersion = jMapper.getDestination(this)
    cddaVersion.cddaMods.addAll(coroutineScope {
      cddaMods.map {
        async {
          val cddaMod = it.toEntity(factory)
          cddaMod.cddaVersion = cddaVersion
          cddaMod
        }
      }.awaitAll()
    })
    return cddaVersion
  }

  companion object {
    @JvmStatic
    fun of(tag: GitTagDto, release: GitHubReleaseDto): CddaVersionParseDto {
      if (tag.name != release.tagName) throw Exception("Tag and release not match")
      val result = CddaVersionParseDto()
      result.releaseName = release.name
      result.commitHash = release.commitHash
      result.experiment = release.isExperiment
      result.releaseDate = release.date.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()

      result.tagName = tag.name
      result.tagDate = tag.date

      result.status = CddaVersionStatus.STOP
      return result
    }
  }
}
