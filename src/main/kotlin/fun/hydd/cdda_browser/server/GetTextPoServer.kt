package `fun`.hydd.cdda_browser.server

import `fun`.hydd.cdda_browser.model.dao.FileEntityDao
import `fun`.hydd.cdda_browser.model.entity.CddaVersion
import `fun`.hydd.cdda_browser.model.entity.FileEntity
import `fun`.hydd.cdda_browser.model.entity.GetTextPo
import io.vertx.core.file.FileSystem
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.hibernate.reactive.stage.Stage.SessionFactory
import java.nio.file.Paths
import java.security.MessageDigest

object GetTextPoServer {
  suspend fun getTextPosByRepo(
    fileSystem: FileSystem,
    factory: SessionFactory,
    repoPath: String,
    cddaVersion: CddaVersion
  ): MutableSet<GetTextPo> = coroutineScope {
    Paths.get(repoPath, "lang", "po").toFile().listFiles()!!
      .filter { it.isFile && it.name.endsWith(".po") }
      .map { Pair(it.name.replace("_", "-").replace(".po", ""), it.absolutePath) }
      .map {
        async { getTextPo(fileSystem, factory, cddaVersion, it.first, it.second) }
      }.awaitAll().toMutableSet()
  }

  private suspend fun getTextPo(
    fileSystem: FileSystem,
    factory: SessionFactory,
    cddaVersion: CddaVersion,
    language: String,
    path: String
  ): GetTextPo {
    val po = GetTextPo()
    po.version = cddaVersion
    po.language = language
    val buffer = fileSystem.readFile(path).await().bytes
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
    return po
  }
}
