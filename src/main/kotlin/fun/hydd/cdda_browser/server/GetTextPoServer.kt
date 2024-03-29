package `fun`.hydd.cdda_browser.server

import `fun`.hydd.cdda_browser.model.dao.FileEntityDao
import `fun`.hydd.cdda_browser.model.entity.FileEntity
import `fun`.hydd.cdda_browser.model.entity.GetTextPo
import `fun`.hydd.cdda_browser.util.ProcessUtil
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
  ): MutableSet<GetTextPo> = coroutineScope {
    Paths.get(repoPath, "lang", "po").toFile().listFiles()!!
      .filter { it.isFile && it.name.endsWith(".po") }
      .map { Pair(it.name.replace("_", "-").replace(".po", ""), it.absolutePath) }
      .map {
        async { getTextPo(fileSystem, factory, it.first, it.second) }
      }.awaitAll().toMutableSet()
  }

  private suspend fun getTextPo(
    fileSystem: FileSystem,
    factory: SessionFactory,
    language: String,
    path: String
  ): GetTextPo {
    val po = GetTextPo()
    po.language = language
    val jsonPath = poFileToJsonFile(path, language)
    val buffer = fileSystem.readFile(jsonPath).await().bytes
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

  private suspend fun poFileToJsonFile(poFile: String, languageCode: String): String {
    val outFileFile = Paths.get(
      System.getProperty("user.home"), "tempDir",
      "$languageCode.json"
    ).toFile()
    if (!outFileFile.parentFile.exists()) {
      outFileFile.parentFile.mkdirs()
    }
    ProcessUtil.po2Json(poFile, outFileFile.absolutePath)
    return outFileFile.absolutePath
  }
}
