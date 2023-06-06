package `fun`.hydd.cdda_browser.model

import `fun`.hydd.cdda_browser.util.extension.getSet
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

data class CddaModJsonEntity(
  val id: String,
  val name: String,
  val description: String,
  val category: String?,
  val dependencies: Set<String>,
  val authors: Set<String>,
  val maintainers: Set<String>,
  val version: String?,
  val path: String?,
  val core: Boolean,
  val obsolete: Boolean,
  val realPath: Path,
) {
  fun getAllDependencies(modMap: Map<String, CddaModJsonEntity>): Set<String> {
    val result = HashSet<String>(this.dependencies)
    result.addAll(this.dependencies.mapNotNull { modMap[it] }.flatMap { it.getAllDependencies(modMap) }.toMutableList())
    return result
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun of(jsonObject: JsonObject, realPath: Path): CddaModJsonEntity {
      val id = jsonObject.getString("id")
      val name = jsonObject.getString("name")
      val description = jsonObject.getString("description")
      val category = jsonObject.getString("category")
      val dependencies = jsonObject.getSet<String>("dependencies") ?: mutableSetOf()
      val authors = jsonObject.getSet<String>("authors") ?: mutableSetOf()
      val maintainers = jsonObject.getSet<String>("maintainers") ?: mutableSetOf()
      val version = jsonObject.getString("version")
      val path = jsonObject.getString("path")
      val core = jsonObject.getBoolean("core") ?: false
      val obsolete = jsonObject.getBoolean("obsolete") ?: false
      return CddaModJsonEntity(
        id,
        name,
        description,
        category,
        dependencies,
        authors,
        maintainers,
        version,
        path,
        core,
        obsolete,
        realPath
      )
    }

    suspend fun getCddaModJsonEntityList(fileSystem: FileSystem, repoPath: String): List<CddaModJsonEntity> =
      coroutineScope {
        val cddaMods = getModDirList(repoPath).map { modDir ->
          async { of(getModInfoJsonObjectByModDir(fileSystem, modDir), modDir.toPath()) }
        }.awaitAll()
        sortMods(cddaMods)
      }

    private fun getModDirList(repoPath: String): List<File> {
      val modsPath = Paths.get(repoPath, "data", "mods").toFile()
      return modsPath.listFiles()!!.filter { it.isDirectory }
    }

    private suspend fun getModInfoJsonObjectByModDir(fileSystem: FileSystem, modDir: File): JsonObject {
      val modinfoFile = modDir.listFiles()?.first { "modinfo.json" == it.name }
      return if (modinfoFile != null) {
        fileSystem.readFile(modinfoFile.absolutePath).await().toJsonArray()
          .mapNotNull { if (it is JsonObject) it else null }.first {
            it.containsKey("type") && it.getString("type").lowercase() == "mod_info"
          }
      } else {
        throw Exception("no find modinfo.json in dir ${modDir.absoluteFile}")
      }
    }

    private fun sortMods(mods: List<CddaModJsonEntity>): List<CddaModJsonEntity> {
      val resultModIds = mutableListOf<String>()
      val myMods = mods.map { Pair(it.id, it.dependencies.toMutableList()) }.toMutableList()
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
  }
}
