package `fun`.hydd.cdda_browser.util

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevTag
import org.eclipse.jgit.revwalk.RevWalk
import org.slf4j.LoggerFactory
import java.util.*

/**
 * utils for handle git repo
 */
object GitUtil {
  private val log = LoggerFactory.getLogger(this.javaClass)

  /**
   * update git repo
   *
   * @param git Git
   */
  fun update(git: Git) {
    log.info("Start update")
    git.pull().setRemote(Constants.DEFAULT_REMOTE_NAME).setRemoteBranchName(Constants.MASTER).call()
    log.info("End update")
  }

  /**
   * rest git repo to tag
   *
   * @param git Git
   * @param tagName rest to tag name
   */
  fun hardRestToTag(git: Git, tagName: String) {
    log.info("Start rest to tag name: $tagName")
    git.reset()
      .setMode(ResetCommand.ResetType.HARD)
      .setRef(tagName)
      .call()
    log.info("End rest to tag name: $tagName")
  }

  /**
   * Ref convert to GitTagDto
   */
  fun getRevObject(git: Git, tagRef: Ref): RevObject {
    val revWalk = RevWalk(git.repository)
    return revWalk.parseAny(tagRef.objectId)
  }

  /**
   * return repo latest tagRef
   */
  fun getLatestTagRef(git: Git): Ref? {
    var result: Ref? = null
    var latestDate: Date? = null
    val tagRefs = git.tagList().call()
    for (tagRef in tagRefs) {
      val currentDate: Date = getTagRefDate(git, tagRef)
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
  private fun getTagRefDate(git: Git, tagRef: Ref): Date {
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
