package `fun`.hydd.cdda_browser.verticles

import `fun`.hydd.cdda_browser.dao.CddaVersionDao
import `fun`.hydd.cdda_browser.dao.FileEntityDao
import `fun`.hydd.cdda_browser.dao.JsonEntityDao
import `fun`.hydd.cdda_browser.dto.GitHubReleaseDto
import `fun`.hydd.cdda_browser.dto.GitTagDto
import `fun`.hydd.cdda_browser.entity.CddaVersion
import `fun`.hydd.cdda_browser.entity.FileEntity
import `fun`.hydd.cdda_browser.entity.GetTextPo
import `fun`.hydd.cdda_browser.server.CddaItemParseManager
import `fun`.hydd.cdda_browser.server.ModServer
import `fun`.hydd.cdda_browser.util.GitUtil
import `fun`.hydd.cdda_browser.util.HttpUtil
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.awaitBlocking
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.hibernate.reactive.stage.Stage
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
import java.security.MessageDigest
import java.time.LocalDateTime
import javax.persistence.Persistence


class UpdateVerticle : CoroutineVerticle() {
  private val log = LoggerFactory.getLogger(this.javaClass)
  private lateinit var git: Git
  private lateinit var factory: Stage.SessionFactory
  private val repoDir: File = Paths.get(System.getProperty("user.home"), "Documents", "Cataclysm-DDA").toFile()

  override suspend fun start() {
    super.start()
    init()
//    GitUtil.update(git)
    val updateVersionList = getNeedUpdateVersions()
    log.info("Need update version size is ${updateVersionList.size}")
    for (cddaVersion in updateVersionList) {
//      GitUtil.hardRestToTag(git, cddaVersion.tagName!!)
      val cddaModDtoList = ModServer.getCddaModDtoList(repoDir.absolutePath, vertx.fileSystem())
      val cddaItemParseManager = CddaItemParseManager(vertx, cddaVersion, cddaModDtoList)
      cddaItemParseManager.parseAll(factory)

      val codePathPairs = Paths.get(repoDir.absolutePath, "lang", "po").toFile().listFiles()
        ?.filter { it.isFile && it.name.endsWith(".po") }
        ?.map { Pair(it.name.replace("_", "-").replace(".po", ""), it.absolutePath) }
      cddaVersion.pos = codePathPairs?.map {
        val po = GetTextPo()
        po.version = cddaVersion
        po.language = it.first
        val buffer = vertx.fileSystem().readFile(it.second).await().bytes
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hash = messageDigest.digest(buffer)
        val hashCode = hash.fold("") { str, byte -> str + "%02x".format(byte) }
        var fileEntity = FileEntityDao.findByHashCode(factory, hashCode)
        if (fileEntity == null) {
          fileEntity = FileEntity()
          fileEntity.buffer = buffer.toTypedArray()
          fileEntity.hashCode = hashCode
        }
        po.fileEntity = fileEntity
        po
      }!!.toMutableSet()
      CddaVersionDao.save(factory, cddaVersion)
      log.info("\n" + JsonEntityDao.first(factory)!!.json!!.encodePrettily())
    }
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
    git = initGitRepo()
    log.info("End init")
  }

  /**
   * Init git repo
   *
   * @return
   */
  private suspend fun initGitRepo(): Git {
    log.info("Start initGit")
    log.info("Repo path is $repoDir")
    return vertx.executeBlocking {
      if (repoDir.exists()) {
        log.info("Repo is exists")
        it.complete(Git.open(repoDir))
      } else {
        log.info("Repo is not exists")
        it.complete(
          Git.cloneRepository().setDirectory(repoDir).setURI("https://github.com/CleverRaven/Cataclysm-DDA.git")
            .setBranch(Constants.MASTER).call()
        )
      }
      log.info("End initGit")
    }.await()
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
    val tagRef = GitUtil.getLatestTagRef(git)
    val gitTagDto = if (tagRef != null) GitTagDto(GitUtil.getRevObject(git, tagRef)) else null
    return if (gitTagDto != null) getCddaVersionByGitTagDto(gitTagDto) else null
  }

  /**
   * Returns the CddaVersion list after the specified time (not include CddaVersion of specified time)
   *
   * @param date
   * @return
   */
  private suspend fun getNeedUpdateVersionList(date: LocalDateTime): List<CddaVersion> {
    val localRefs = git.tagList().call()
    val result = ArrayList<GitTagDto>()
    for (localRef in localRefs) {
      val gitTagDto = GitTagDto(GitUtil.getRevObject(git, localRef))
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
