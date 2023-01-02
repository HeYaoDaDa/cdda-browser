package `fun`.hydd.cdda_browser.verticles

import `fun`.hydd.cdda_browser.dto.GitHubReleaseDto
import `fun`.hydd.cdda_browser.dto.GitTagDto
import `fun`.hydd.cdda_browser.entity.CddaVersion
import `fun`.hydd.cdda_browser.entity.FileEntity
import `fun`.hydd.cdda_browser.entity.GetTextPo
import `fun`.hydd.cdda_browser.entity.JsonEntity
import `fun`.hydd.cdda_browser.server.CddaItemParseManager
import `fun`.hydd.cdda_browser.server.ModServer
import `fun`.hydd.cdda_browser.util.HttpUtil
import io.vertx.core.Future
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.awaitBlocking
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import org.hibernate.reactive.stage.Stage
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.CompletionStage
import javax.persistence.Persistence


class UpdateVerticle : CoroutineVerticle() {
  private val log = LoggerFactory.getLogger(this.javaClass)
  private lateinit var git: Git
  private lateinit var factory: Stage.SessionFactory
  private val repositoryPath = Paths.get(System.getProperty("user.home"), "Documents", "Cataclysm-DDA").toFile()


  override suspend fun start() {
    super.start()
    init()
//    update()
    val updateVersionList = getUpdateVersionList()
    log.info("Need update version size is ${updateVersionList.size}")
    for (cddaVersion in updateVersionList) {
//      rest(cddaVersion.tagName!!)
      val cddaModDtoList = ModServer.getCddaModDtoList(repositoryPath.absolutePath, vertx.fileSystem())
      val cddaItemParseManager = CddaItemParseManager(vertx, cddaVersion, cddaModDtoList)
      cddaItemParseManager.parseAll(factory)

      val codePathPairs = Paths.get(repositoryPath.absolutePath, "lang", "po").toFile().listFiles()
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
        var fileEntity = findFileEntity(factory, hashCode)
        if (fileEntity == null) {
          fileEntity = FileEntity()
          fileEntity.buffer = buffer.toTypedArray()
          fileEntity.hashCode = hashCode
        }
        po.fileEntity = fileEntity
        po
      }!!.toMutableSet()
      saveVersion(cddaVersion)
      val test = factory.withSession {
        it.createQuery<JsonEntity>("FROM JsonEntity").setMaxResults(1).singleResult
      }.await()
      log.info("\n" + test.json!!.encodePrettily())
    }
  }

  private suspend fun findFileEntity(factory: Stage.SessionFactory, hashCode: String): FileEntity? {
    return factory.withSession {
      it.createQuery<FileEntity>("FROM FileEntity where hashCode = \'$hashCode\'").singleResultOrNull
    }.await()
  }

  suspend fun saveVersion(version: CddaVersion) {
    factory.withTransaction { session, _ -> session.persist(version) }.await()
  }

  fun <T> CompletionStage<T>.toFuture(): Future<T> {
    return Future.fromCompletionStage(this)
  }

  suspend fun <T> CompletionStage<T>.await(): T {
    return this.toFuture().await()
  }

  private suspend fun getUpdateVersionList(): List<CddaVersion> {
    val updateVersionList = mutableListOf<CddaVersion>()
    val repoLatestVersionDto = getRepoLatestVersionDto()
    if (repoLatestVersionDto != null) {
      val dbLatestVersionDto = getLatestVersion()
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

  private suspend fun init() {
    log.info("Start init")
    factory = awaitBlocking {
      Persistence.createEntityManagerFactory("cdda-browser").unwrap(Stage.SessionFactory::class.java)
    }
    git = initGit()
    log.info("End init")
  }

  /**
   * Init git repo
   *
   * @return
   */
  private suspend fun initGit(): Git {
    log.info("Start initGit")
    log.info("Repo path is $repositoryPath")
    return vertx.executeBlocking {
      if (repositoryPath.exists()) {
        log.info("Repo is exists")
        it.complete(Git.open(repositoryPath))
      } else {
        log.info("Repo is not exists")
        it.complete(
          Git.cloneRepository().setDirectory(repositoryPath).setURI("https://github.com/CleverRaven/Cataclysm-DDA.git")
            .setBranch(Constants.MASTER).call()
        )
      }
      log.info("End initGit")
    }.await()
  }

  /**
   * Return latest CddaVersionDto for current repo status
   */
  private suspend fun getRepoLatestVersionDto(): CddaVersion? {
    val gitTagDto = getLatestGitTagDto()
    return if (gitTagDto != null) {
      tag2CddaVersion(gitTagDto)
    } else {
      null
    }
  }

  /**
   * GitTag convert to CddaVersion
   *
   * @param tag
   * @return
   */
  private suspend fun tag2CddaVersion(tag: GitTagDto): CddaVersion {
    val releaseDto = getReleaseByTagName(tag.name)
    return CddaVersion.of(tag, releaseDto)
  }

  /**
   * return latest cdda version in db
   */
  private suspend fun getLatestVersion(): CddaVersion? {
    return Future.fromCompletionStage(factory.withSession {
      it.createQuery<CddaVersion>("FROM CddaVersion ORDER BY releaseDate DESC").setMaxResults(1).singleResultOrNull
    }).await()
  }

  /**
   * By tag name get GitHub GithubReleaseDto
   */
  private suspend fun getReleaseByTagName(tagName: String): GitHubReleaseDto {
    val requestOptions: RequestOptions =
      RequestOptions().setHost("api.github.com").setURI("/repos/CleverRaven/Cataclysm-DDA/releases/tags/$tagName")
        .setMethod(HttpMethod.GET).setPort(443).putHeader("User-Agent", "item-browser").setSsl(true)
    val buffer = HttpUtil.request(vertx, requestOptions)
    return if (buffer != null) {
      val jsonObject: JsonObject = buffer.toJsonObject()
      jsonObject.mapTo(GitHubReleaseDto::class.java)
    } else {
      throw Exception("Tag $tagName release response is null")
    }
  }

  /**
   * update git repo
   */
  private suspend fun update() {
    log.info("Start update")
    //TODO out time issue
    awaitBlocking {
      git.pull().setRemote(Constants.DEFAULT_REMOTE_NAME).setRemoteBranchName(Constants.MASTER).call()
    }
    log.info("End update")
  }

  /**
   * rest git repo to tag
   */
  private suspend fun rest(tagName: String) {
    log.info("Start rest to $tagName")
    awaitBlocking {
      git.reset().setMode(ResetCommand.ResetType.HARD)
        .setRef(tagName)
        .call()
    }
    log.info("End rest to $tagName")
  }

  /**
   * Returns the CddaVersion list after the specified time (not include CddaVersion of specified time)
   *
   * @param date
   * @return
   */
  private suspend fun getNeedUpdateVersionList(date: LocalDateTime): List<CddaVersion> {
    val afterTagList = getAfterTagList(date)
    return afterTagList.map { tag2CddaVersion(it) }
  }

  /**
   * return repo latest GitTagDto
   */
  private fun getLatestGitTagDto(): GitTagDto? {
    val tagRef = getLatestTagRef()
    return if (tagRef != null)
      tagRef2GitTagDto(tagRef)
    else null
  }

  /**
   * Returns the GitTagDto list after the specified time (not include GitTagDto of specified time)
   */
  private fun getAfterTagList(date: LocalDateTime): List<GitTagDto> {
    val localRefs = git.tagList()
      .call()
    val result = ArrayList<GitTagDto>()
    for (localRef in localRefs) {
      val gitTagDto = tagRef2GitTagDto(localRef)
      if (gitTagDto.date.isAfter(date)) {
        result.add(gitTagDto)
      }
    }
    return result.sortedBy { it.date }
  }

  /**
   * Ref convert to GitTagDto
   */
  private fun tagRef2GitTagDto(tagRef: Ref): GitTagDto {
    RevWalk(git.repository).use { revWalk ->
      val revObject = revWalk.parseAny(tagRef.objectId)
      if (Constants.OBJ_TAG == revObject.type) {
        val revTag = revObject as RevTag
        return GitTagDto(
          revTag.tagName, revTag.taggerIdent.getWhen().toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()
        )
      } else if (Constants.OBJ_COMMIT == revObject.type) {
        val revCommit = revObject as RevCommit
        return GitTagDto(
          revCommit.name, revCommit.authorIdent.getWhen().toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()
        )
      } else {
        throw Exception("Wrong Ref type, not OBJ_TAG or OBJ_COMMIT")
      }
    }
  }

  /**
   * return repo latest tagRef
   */
  private fun getLatestTagRef(): Ref? {
    var result: Ref? = null
    var latestDate: Date? = null
    val tagRefs = git.tagList().call()
    for (tagRef in tagRefs) {
      val currentDate: Date = getTagRefDate(tagRef)
      if (latestDate == null || currentDate.after(latestDate)) {
        result = tagRef
        latestDate = currentDate
      }
    }
    return result
  }

  /**
   * get tagRef date
   * for annotated tag is tag date
   * for lightweight tag is commit date
   */
  private fun getTagRefDate(tagRef: Ref): Date {
    RevWalk(git.repository).use { revWalk ->
      val revObject = revWalk.parseAny(tagRef.objectId)
      if (Constants.OBJ_TAG == revObject.type) {
        val revTag = revObject as RevTag
        return revTag.taggerIdent.getWhen()
      } else if (Constants.OBJ_COMMIT == revObject.type) {
        val revCommit = revObject as RevCommit
        return revCommit.authorIdent.getWhen()
      } else {
        throw Exception("Wrong Ref type, not OBJ_TAG or OBJ_COMMIT")
      }
    }
  }
}
