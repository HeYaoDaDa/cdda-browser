package `fun`.hydd.cdda_browser.model.bo.parse

import `fun`.hydd.cdda_browser.constant.CddaType
import `fun`.hydd.cdda_browser.constant.JsonType
import `fun`.hydd.cdda_browser.util.JsonUtil
import `fun`.hydd.cdda_browser.util.extension.getCollection
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths

/**
 * A DTO for the {@link fun.hydd.cdda_browser.entity.CddaMod} entity
 */
class CddaParseMod {
  lateinit var id: String

  lateinit var name: String

  lateinit var description: String

  var obsolete: Boolean = false

  var core: Boolean = false

  lateinit var depModIds: Collection<String>

  lateinit var path: Collection<File>

  lateinit var depMods: Set<CddaParseMod>

  lateinit var cddaParsedJsons: Set<CddaParsedJson>

  val cddaItems: MutableCollection<CddaParseItem> = mutableSetOf()

  val cddaItemSize: Int by lazy {
    cddaParsedJsons.size
  }

  val allDepMods: Set<CddaParseMod> by lazy {
    depMods.flatMap { it.allDepMods + it }.toHashSet()
  }

  val allDepModIds: Set<String> by lazy {
    allDepMods.map { it.id }.toHashSet()
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    /**
     * Get repo /data/mods sored mod dto list
     *
     * @param repoPath repo path
     * @param fileSystem vertx FileSystem
     * @return sorted CddaModDto list
     */
    suspend fun getCddaModDtoList(fileSystem: FileSystem, repoPath: String): List<CddaParseMod> = coroutineScope {
      log.info("Start getCddaModDtoList")
      val missJsonTypes = mutableSetOf<String>()
      val cddaMods = getModDirList(repoPath).map {
        async {
          val mod = parserCddaModJsonObject(findModInfoJsonObjectByModDir(fileSystem, it), it)
          if (mod != null) mod.cddaParsedJsons = getCddaJsonsByMod(fileSystem, mod, missJsonTypes)
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
    private suspend fun parserCddaModJsonObject(jsonObject: JsonObject?, modDir: File): CddaParseMod? {
      if (jsonObject == null) return null
      if (!JsonType.MOD_INFO.isEquals(jsonObject.getString("type"))) throw IllegalArgumentException("jsonObject type not is MOD_INFO")
      val modPaths = mutableSetOf(modDir)
      if (jsonObject.containsKey("path")) {
        val path = jsonObject.getString("path", ".")
        modPaths.add(withContext(Dispatchers.IO) {
          modDir.toPath().resolve(path).toRealPath()
        }.toFile())
      }
      val cddaModDto = CddaParseMod()
      cddaModDto.id = jsonObject.getString("id")
      cddaModDto.name = jsonObject.getString("name")
      cddaModDto.description = jsonObject.getString("description")
      cddaModDto.obsolete = jsonObject.getBoolean("obsolete", false)
      cddaModDto.core = jsonObject.getBoolean("core", false)
      cddaModDto.depModIds = jsonObject.getCollection<String>("dependencies", listOf()).toHashSet()
      cddaModDto.path = modPaths
      return cddaModDto
    }

    /**
     * get mod path's CddaJson
     *
     * @param fileSystem
     * @param mod
     */
    private suspend fun getCddaJsonsByMod(
      fileSystem: FileSystem,
      mod: CddaParseMod,
      missJsonTypes: MutableSet<String>
    ) =
      coroutineScope {
        mod.path.flatMap { getAllJsonFileInDir(it) }.map { jsonFile ->
          async {
            JsonUtil.getJsonObjectsByFile(fileSystem, jsonFile).map { parseCddaJson(it, mod, jsonFile, missJsonTypes) }
          }
        }.awaitAll().flatten().filterNotNull().toSet()
      }

    private fun parseCddaJson(
      jsonObject: JsonObject,
      mod: CddaParseMod,
      path: File,
      missJsonTypes: MutableSet<String>
    ): CddaParsedJson? {
      return if (jsonObject.containsKey("type")) {
        val type = jsonObject.getString("type")
        if (missJsonTypes.contains(type)) return null
        var cddaType: CddaType? = null
        var jsonType: JsonType? = null
        for (cddaTypeEntry in CddaType.values()) {
          for (jsonTypeEntry in cddaTypeEntry.jsonType) {
            if (jsonTypeEntry.isEquals(type)) {
              cddaType = cddaTypeEntry
              jsonType = jsonTypeEntry
              break
            }
          }
        }
        if (cddaType != null && jsonType != null) {
          val cddaParsedJson = CddaParsedJson()
          cddaParsedJson.jsonType = jsonType
          cddaParsedJson.cddaType = cddaType
          cddaParsedJson.mod = mod
          cddaParsedJson.path = path
          cddaParsedJson.json = jsonObject
          cddaParsedJson.copyFrom = jsonObject.getString("copy-from")
          cddaParsedJson.abstract = jsonObject.getBoolean("abstract", false)
          cddaParsedJson.relative = jsonObject.getJsonObject("relative")
          cddaParsedJson.proportional = jsonObject.getJsonObject("proportional")
          cddaParsedJson.extend = jsonObject.getJsonObject("extend")
          cddaParsedJson.delete = jsonObject.getJsonObject("delete")
          cddaParsedJson
        } else {
          missJsonTypes.add(type)
          log.warn("$type not exits in JsonType")
          null
        }
      } else {
        null
      }
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
    private fun sortMods(mods: List<CddaParseMod>): List<CddaParseMod> {
      val resultModIds = mutableListOf<String>()
      val myMods = mods.sortedBy { it.id }.map { Pair(it.id, it.depModIds.toMutableList()) }.toMutableList()
      while (myMods.isNotEmpty()) {
        val beforeSize = myMods.size
        myMods.filter { it.second.isEmpty() }.map { resultModIds.add(it.first);myMods.remove(it) }
        myMods.map { it.second.removeIf { depModId -> resultModIds.contains(depModId) } }
        if (myMods.size == beforeSize) {
          break
        }
      }
      return resultModIds.map { modId -> mods.first { it.id == modId } }
    }

    /**
     * set mods depMods, mods must sored
     * @param soredMods List<CddaMod>
     */
    private fun setAllModDepMods(soredMods: List<CddaParseMod>) {
      val modMap = soredMods.associateBy { it.id }
      soredMods.forEach { superMod -> superMod.depMods = superMod.depModIds.mapNotNull { modMap[it] }.toHashSet() }
    }
  }
}
