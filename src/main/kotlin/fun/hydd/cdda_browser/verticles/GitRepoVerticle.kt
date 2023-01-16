package `fun`.hydd.cdda_browser.verticles

import `fun`.hydd.cdda_browser.constant.EventBusConstant
import io.vertx.kotlin.coroutines.CoroutineVerticle
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
import java.util.*

class GitRepoVerticle : CoroutineVerticle() {
  private val repoDir: File = Paths.get(System.getProperty("user.home"), "Documents", "Cataclysm-DDA").toFile()
  private val repoRemoteURL = "https://github.com/CleverRaven/Cataclysm-DDA.git"
  private lateinit var git: Git

  private val log = LoggerFactory.getLogger(this.javaClass)


  override suspend fun start() {
    super.start()
    init()
    val eventBus = vertx.eventBus()
    eventBus.consumer<Unit>(EventBusConstant.GIT_REPO_UPDATE) {
      update()
    }
    eventBus.consumer(EventBusConstant.GIT_REPO_HARD_REST_TO_TAG) {
      hardRestToTag(it.body())
    }
    eventBus.consumer(EventBusConstant.GIT_REPO_GET_REV_OBJECT) {
      it.reply(getRevObject(it.body()))
    }
    eventBus.consumer<Unit>(EventBusConstant.GIT_REPO_GET_LATEST_REV_OBJECT) {
      it.reply(getLatestRevObject())
    }
    eventBus.consumer<Unit>(EventBusConstant.GIT_REPO_GET_TAG_LIST) {
      it.reply(getTagList())
    }
  }

  override suspend fun stop() {
    super.stop()
    git.close()
  }

  /**
   * Init Cdda Git Repo
   *
   */
  private fun init() {
    git = if (repoDir.exists()) {
      log.info("Repo is exists")
      Git.open(repoDir)
    } else {
      log.info("Repo is not exists")
      Git.cloneRepository()
        .setDirectory(repoDir)
        .setURI(repoRemoteURL)
        .setBranch(Constants.MASTER).call()
    }
    log.info("End initGit")
  }

  /**
   * update git repo
   *
   */
  private fun update() {
    log.info("Start update")
    git.pull().setRemote(Constants.DEFAULT_REMOTE_NAME).setRemoteBranchName(Constants.MASTER).call()
    log.info("End update")
  }

  /**
   * rest git repo to tag
   *
   * @param tagName rest to tag name
   */
  private fun hardRestToTag(tagName: String) {
    log.info("Start rest to tag name: $tagName")
    git.reset()
      .setMode(ResetCommand.ResetType.HARD)
      .setRef(tagName)
      .call()
    log.info("End rest to tag name: $tagName")
  }

  /**
   * Get return repo latest RevObject
   *
   * @return
   */
  private fun getLatestRevObject(): RevObject? {
    val latestTagRef = getLatestTagRef()
    return if (latestTagRef == null) null else getRevObject(latestTagRef)
  }

  /**
   * Ref convert to RevObject
   */
  private fun getRevObject(tagRef: Ref): RevObject {
    val revWalk = RevWalk(git.repository)
    return revWalk.parseAny(tagRef.objectId)
  }

  private fun getTagList(): List<Ref> {
    return git.tagList().call()
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
   *
   * @param tagRef git tag ref ( annotated tag or lightweight tag )
   * @return tag date or commit date ( lightweight tag )
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
