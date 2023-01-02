package `fun`.hydd.cdda_browser.server

import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.dto.CddaJson
import `fun`.hydd.cdda_browser.dto.CddaModDto
import `fun`.hydd.cdda_browser.extension.getCollection
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths

object ModServer {
  private val log = LoggerFactory.getLogger(this.javaClass)

  /**
   * Get repo /data/mods mod dto
   *
   * @param repoPath repo path
   * @param fileSystem vertx FileSystem
   * @return sorted CddaModDto list
   */
  suspend fun getCddaModDtoList(repoPath: String, fileSystem: FileSystem): List<CddaModDto> = coroutineScope {
    log.info("Start getCddaModDtoList")
    val cddaMods = getModDirList(repoPath).map {
      async {
        parserCddaModJsonObject(findModInfoJsonObjectByModDir(it, fileSystem), fileSystem, it, repoPath)
      }
    }.awaitAll().mapNotNull { it }
    val sortedMods = sortMods(cddaMods)
    setAllModDepMods(sortedMods)
    log.info("End getCddaModDtoList")
    sortedMods
  }

  /**
   * Find all modinfo.json file by repo path
   *
   * @param repoPath
   * @return mod directory list
   */
  private fun getModDirList(repoPath: String): List<File> {
    val modsPath = Paths.get(repoPath, "data", "mods").toFile()
    return modsPath.listFiles()!!.filter { it.isDirectory }
  }

  private suspend fun findModInfoJsonObjectByModDir(modDir: File, fileSystem: FileSystem): JsonObject? {
    val modinfoFile = modDir.listFiles()?.first { "modinfo.json" == it.name }
    return if (modinfoFile != null) {
      fileSystem.readFile(modinfoFile.absolutePath).await().toJsonArray()
        .mapNotNull { if (it is JsonObject) it else null }.first {
          it.containsKey("type") && it.getString("type").lowercase() == "mod_info"
        }
    } else {
      log.warn("${modDir.absoluteFile} no find modinfo.json")
      null
    }
  }

  private suspend fun parserCddaModJsonObject(
    jsonObject: JsonObject?,
    fileSystem: FileSystem,
    modDir: File,
    repoPath: String
  ): CddaModDto? {
    if (jsonObject == null) return null
    if (!JsonType.MOD_INFO.isEquals(jsonObject.getString("type"))) throw IllegalArgumentException("jsonObject type not is MOD_INFO")
    val modPaths = mutableSetOf(modDir)
    if (jsonObject.containsKey("path")) {
      val path = jsonObject.getString("path", ".")
      modPaths.add(withContext(Dispatchers.IO) {
        modDir.toPath().resolve(path).toRealPath()
      }.toFile())
    }
    val mod = CddaModDto(
      jsonObject.getString("id"),
      jsonObject.getString("name"),
      jsonObject.getString("description"),
      jsonObject.getBoolean("obsolete", false),
      jsonObject.getBoolean("core", false),
      jsonObject.getCollection<String>("dependencies", listOf()).toHashSet(),
      modPaths
    )
    mod.cddaJsons = coroutineScope {
      mod.path.flatMap { getAllJsonFileInDir(it) }.map { file ->
        async {
          getJsonObjectsByFile(fileSystem, file).map { CddaJson.of(it, mod, file) }
        }
      }.awaitAll().flatten().filterNotNull().toSet()
    }
    return mod
  }

  private suspend fun getJsonObjectsByFile(fileSystem: FileSystem, file: File): List<JsonObject> = coroutineScope {
    when (val buffer = fileSystem.readFile(file.absolutePath).await().toJson()) {
      is JsonArray -> {
        buffer.mapNotNull { if (it is JsonObject) it else null }
      }

      is JsonObject -> {
        listOf(buffer)
      }

      else -> {
        throw Exception("Return not is JsonObject or JsonArray")
      }
    }
  }

  private fun getAllJsonFileInDir(dirPath: File): List<File> {
    val result: MutableList<File> = ArrayList()
    val dirFiles = dirPath.listFiles()
    if (dirFiles != null && dirFiles.isNotEmpty()) {
      for (file in dirFiles) {
        if (file.isDirectory) {
          result.addAll(getAllJsonFileInDir(file))
        } else if (file.absolutePath.endsWith(".json")) {
          result.add(file)
        }
      }
    }
    return result
  }

  /**
   * topology Sort by dependent modId, no change param
   * @param mods List<CddaMod>
   * @return List<CddaMod>
   */
  private fun sortMods(mods: List<CddaModDto>): List<CddaModDto> {
    val resultModIds = mutableListOf<String>();
    val myMods = mods.map { Pair(it.modId, it.depModIds.toMutableList()) }.toMutableList()
    while (myMods.isNotEmpty()) {
      val beforeSize = myMods.size
      myMods.filter { it.second.isEmpty() }.map { resultModIds.add(it.first as String);myMods.remove(it) }
      myMods.map { it.second.removeIf { depModId -> resultModIds.contains(depModId) } }
      if (myMods.size == beforeSize) {
        break
      }
    }
    return resultModIds.map { modId -> mods.first { it.modId == modId } }
  }

  /**
   * set mods depMods, mods must sored
   * @param soredMods List<CddaMod>
   */
  private fun setAllModDepMods(soredMods: List<CddaModDto>) {
    val modMap = soredMods.associateBy { it.modId }
    soredMods.forEach { superMod -> superMod.depMods = superMod.depModIds.mapNotNull { modMap[it] }.toHashSet() }
  }
}
