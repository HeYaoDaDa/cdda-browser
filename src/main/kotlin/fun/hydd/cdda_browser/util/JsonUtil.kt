package `fun`.hydd.cdda_browser.util

import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.util.extension.parse
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import java.io.File
import kotlin.collections.set
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.superclasses
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

object JsonUtil {
  /**
   * Get json file all JsonObject
   *
   * @param fileSystem vertx fileSystem
   * @param file json file
   * @return JsonObjects
   */
  suspend fun getJsonObjectsByFile(fileSystem: FileSystem, file: File): List<JsonObject> {
    return when (val buffer = fileSystem.readFile(file.absolutePath).await().toJson()) {
      is JsonArray -> buffer.mapNotNull { if (it is JsonObject) it else null }
      is JsonObject -> listOf(buffer)
      else -> throw Exception("Result class isn't JsonObject or JsonArray")
    }
  }

  /**
   * recursive sort jsonObject key-value by key, no parameter modification
   * @param jsonObject pending sort JsonObject
   * @return sored JsonObject
   */
  fun sortJsonObject(jsonObject: JsonObject): JsonObject {
    val sortedMap = LinkedHashMap<String, Any?>(jsonObject.size())
    for (entity in jsonObject.map.entries.sortedBy { it.key }) {
      val value = entity.value
      if (value != null) {
        sortedMap[entity.key] = when (value) {
          is JsonObject -> sortJsonObject(value)
          is JsonArray -> sortJsonArray(value)
          else -> value
        }
      } else {
        sortedMap[entity.key] = null
      }
    }
    return JsonObject(sortedMap)
  }

  /**
   * recursive sort jsonArray's JsonObject key-value, detail: [sortJsonObject]
   * @param jsonArray pending sort JsonArray
   * @return sored JsonArray
   */
  fun sortJsonArray(jsonArray: JsonArray): JsonArray {
    return JsonArray(
      jsonArray.toList().stream().map {
        when (it) {
          is JsonObject -> sortJsonObject(it)
          is JsonArray -> sortJsonArray(it)
          else -> it
        }
      }.toList()
    )
  }

  fun javaField2JsonField(fieldName: String): String {
    val result = StringBuilder()
    for (char in fieldName) {
      if (char != char.lowercaseChar()) result.append("_")
      result.append(char.lowercase())
    }
    return result.toString()
  }

  fun parseJsonField(fieldType: KType, jsonValue: Any, param: String): Any {
    val fieldClass = fieldType.jvmErasure
    return if (fieldClass.isSubclassOf(MutableCollection::class)) {
      val subFieldType = fieldType.arguments[0].type!!
      val fieldInstant: MutableCollection<Any> =
        if (fieldClass == MutableSet::class || fieldClass.superclasses.contains(MutableSet::class)) mutableSetOf()
        else mutableListOf()
      fieldInstant.addAll((if (jsonValue is JsonArray) jsonValue else JsonArray.of(jsonValue)).map {
        parseJsonField(subFieldType, it, param)
      })
      fieldInstant
    } else {
      when (fieldClass) {
        String::class -> return String.parse(jsonValue)
        Boolean::class -> return Boolean.parse(jsonValue)
        Double::class -> return Double.parse(jsonValue)
        Int::class -> return Int.parse(jsonValue)
      }
      if (fieldClass.isSubclassOf(Enum::class)) {
        val enumClz = fieldClass.java.enumConstants.filterIsInstance<Enum<*>>()
        return when (jsonValue) {
          is String -> enumClz.find { it.name == jsonValue.uppercase() }
            ?: throw Exception("enum $fieldClass miss $jsonValue")

          is Int -> enumClz[jsonValue]
          else -> throw Exception("$jsonValue is not String or Int")
        }
      } else if (fieldClass.isSubclassOf(CddaSubObject::class)) {
        val fieldInstant = fieldClass.primaryConstructor!!.callBy(emptyMap()) as CddaSubObject
        fieldInstant.parse(jsonValue, param)
        return fieldInstant
      }
      throw Exception("fieldClass(${fieldClass.jvmName}) not baseType or CddaSubObject")
    }
  }

  fun formatDouble(source: Double): Double {
    return "%.2f".format(source).toDouble()
  }

  fun jsonToDouble(jsonValue: Any): Double {
    return when (jsonValue) {
      is Double -> jsonValue
      is Float -> jsonValue.toDouble()
      is Int -> jsonValue.toDouble()
      is Long -> jsonValue.toDouble()
      else -> throw Exception("input $jsonValue is not Double")
    }
  }
}


