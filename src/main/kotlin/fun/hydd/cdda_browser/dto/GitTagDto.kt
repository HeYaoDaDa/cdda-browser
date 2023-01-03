package `fun`.hydd.cdda_browser.dto

import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevObject
import org.eclipse.jgit.revwalk.RevTag
import java.time.LocalDateTime
import java.time.ZoneOffset

class GitTagDto() {
  lateinit var name: String
  lateinit var date: LocalDateTime

  constructor(name: String, date: LocalDateTime) : this() {
    this.name = name
    this.date = date
  }

  /**
   * from jGit RevObject construct, if not tag throw Exception
   */
  constructor(revObject: RevObject) : this() {
    if (Constants.OBJ_TAG == revObject.type) {
      val revTag = revObject as RevTag
      this.name = revTag.tagName
      this.date = revTag.taggerIdent.getWhen().toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()
    } else if (Constants.OBJ_COMMIT == revObject.type) {
      val revCommit = revObject as RevCommit
      this.name = revCommit.name
      this.date = revCommit.authorIdent.getWhen().toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime()
    } else {
      throw Exception("Wrong Ref type, not OBJ_TAG or OBJ_COMMIT")
    }
  }
}
