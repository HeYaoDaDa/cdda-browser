package `fun`.hydd.cdda_browser.server

import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.model.base.CddaJsonParsedResult
import `fun`.hydd.cdda_browser.model.base.CddaModParseDto
import `fun`.hydd.cdda_browser.util.JsonUtil
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths

object ModServer {
  private val log = LoggerFactory.getLogger(this.javaClass)

  /**
   * Get repo /data/mods sored mod dto list
   *
   * @param repoPath repo path
   * @param fileSystem vertx FileSystem
   * @return sorted CddaModDto list
   */
  suspend fun getCddaModDtoList(fileSystem: FileSystem, repoPath: String): List<CddaModParseDto> = coroutineScope {
    log.info("Start getCddaModDtoList")
    val cddaMods = getModDirList(repoPath).map {
      async {
        val mod = parserCddaModJsonObject(findModInfoJsonObjectByModDir(fileSystem, it), it)
        if (mod != null) mod.cddaJsonParsedResults = getCddaJsonsByMod(fileSystem, mod)
        mod
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

  /**
   * find modinfo.json for mod path
   *
   * @param fileSystem
   * @param modDir
   * @return
   */
  private suspend fun findModInfoJsonObjectByModDir(fileSystem: FileSystem, modDir: File): JsonObject? {
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

  /**
   * parser MOD_INFO jsonObject build CddaModDto
   *
   * @param jsonObject
   * @param modDir
   * @return
   */
  private suspend fun parserCddaModJsonObject(jsonObject: JsonObject?, modDir: File): CddaModParseDto? {
    if (jsonObject == null) return null
    if (!JsonType.MOD_INFO.isEquals(jsonObject.getString("type"))) throw IllegalArgumentException("jsonObject type not is MOD_INFO")
    val modPaths = mutableSetOf(modDir)
    if (jsonObject.containsKey("path")) {
      val path = jsonObject.getString("path", ".")
      modPaths.add(withContext(Dispatchers.IO) {
        modDir.toPath().resolve(path).toRealPath()
      }.toFile())
    }
    return CddaModParseDto.of(jsonObject, modPaths)
  }

  /**
   * get mod path's CddaJson
   *
   * @param fileSystem
   * @param mod
   */
  private suspend fun getCddaJsonsByMod(fileSystem: FileSystem, mod: CddaModParseDto) = coroutineScope {
    mod.path.flatMap { getAllJsonFileInDir(it) }.map { jsonFile ->
      async {
        JsonUtil.getJsonObjectsByFile(fileSystem, jsonFile).map { CddaJsonParsedResult.of(it, mod, jsonFile) }
      }
    }.awaitAll().flatten().filterNotNull().toSet()
  }


  /**
   * recursion get all json file for dir
   *
   * @param dirPath
   * @return
   */
  private suspend fun getAllJsonFileInDir(dirPath: File): List<File> = coroutineScope {
    val result: MutableList<File> = ArrayList()
    val dirFiles = dirPath.listFiles()
    if (dirFiles != null && dirFiles.isNotEmpty()) {
      dirFiles.mapNotNull {
        async {
          if (it.isDirectory) result.addAll(getAllJsonFileInDir(it))
          else if (it.absolutePath.endsWith(".json")) result.add(it)
          else null
        }
      }.awaitAll()
    }
    result
  }

  /**
   * topology Sort by dependent modId, no change param
   * @param mods List<CddaMod>
   * @return List<CddaMod>
   */
  private fun sortMods(mods: List<CddaModParseDto>): List<CddaModParseDto> {
    val resultModIds = mutableListOf<String>()
    val myMods = mods.map { Pair(it.modId, it.depModIds.toMutableList()) }.toMutableList()
    while (myMods.isNotEmpty()) {
      val beforeSize = myMods.size
      myMods.filter { it.second.isEmpty() }.map { resultModIds.add(it.first);myMods.remove(it) }
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
  private fun setAllModDepMods(soredMods: List<CddaModParseDto>) {
    val modMap = soredMods.associateBy { it.modId }
    soredMods.forEach { superMod -> superMod.depMods = superMod.depModIds.mapNotNull { modMap[it] }.toHashSet() }
  }
}
