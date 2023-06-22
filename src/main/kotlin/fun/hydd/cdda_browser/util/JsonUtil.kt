package `fun`.hydd.cdda_browser.util

import `fun`.hydd.cdda_browser.annotation.IgnoreMap
import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.InheritData
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.util.extension.parse
import `fun`.hydd.cdda_browser.util.extension.proportional
import `fun`.hydd.cdda_browser.util.extension.relative
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.collections.set
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

object JsonUtil {
  private val log = LoggerFactory.getLogger(this.javaClass)

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

  fun autoLoad(instant: Any, jsonValue: Any, inheritData: InheritData?) {
    instant::class.memberProperties.filterIsInstance<KMutableProperty<*>>().forEach { prop ->
      log.info("\t\t\tprop ${prop.name}")
      val mapInfo = prop.findAnnotations(MapInfo::class).firstOrNull() ?: MapInfo()
      val ignore = prop.findAnnotations(IgnoreMap::class).isNotEmpty()
      val jsonFieldName = mapInfo.key.ifBlank { javaField2JsonField(prop.name) }
      if (!ignore) {
        var result = if (jsonValue is JsonObject) {
          if (jsonValue.containsKey(jsonFieldName)) {
            val realJsonValue = jsonValue.getValue(jsonFieldName)
            if (realJsonValue == null)
              null
            else
              parseJsonField(prop.returnType, realJsonValue, mapInfo.param)
          } else {
            prop.getter.call(instant)
          }
        } else {
          parseJsonField(prop.returnType, jsonValue, mapInfo.param)
        }
        if (inheritData != null) result =
          processInheritData(prop.returnType, result, inheritData, jsonFieldName, mapInfo.param)
        prop.setter.call(instant, result)
      }
      if (mapInfo.spFun.isNotBlank()) {
        val spFun = instant::class.functions.firstOrNull() { it.name == mapInfo.spFun }
          ?: throw Exception("class ${instant::class} spFun ${mapInfo.spFun} is miss")
        val args: MutableMap<KParameter, Any?> = mutableMapOf(spFun.instanceParameter!! to instant)
        spFun.parameters.forEachIndexed { index, kParameter ->
          if (index > 0) {
            args[kParameter] = when (kParameter.name) {
              "json" -> jsonValue
              "fieldValue" -> if (jsonValue is JsonObject) jsonValue.getValue(jsonFieldName) else jsonValue
              else -> throw Exception("miss arg ${kParameter.name}")
            }
          }
        }
        if (spFun.parameters.size == 2) args[spFun.parameters[1]] = jsonValue
        spFun.callBy(args)
      }
    }
  }

  @Suppress("UNCHECKED_CAST")
  private fun processInheritData(
    fieldType: KType,
    currentValue: Any?,
    inheritData: InheritData,
    jsonFieldName: String,
    param: String,
  ): Any? {
    val fieldClass = fieldType.jvmErasure
    if (fieldClass.superclasses.contains(MutableCollection::class)) {
      if (inheritData.extend?.containsKey(jsonFieldName) == true) {
        val extendValue = parseJsonField(
          fieldType,
          inheritData.extend.getValue(jsonFieldName),
          param
        ) as MutableCollection<Any>
        return if (currentValue != null) {
          (currentValue as MutableCollection<Any>).addAll(extendValue)
          currentValue
        } else {
          extendValue
        }
      }
      if (inheritData.delete?.containsKey(jsonFieldName) == true) {
        return if (currentValue != null) {
          val deleteValue = parseJsonField(
            fieldType,
            inheritData.delete.getValue(jsonFieldName),
            param
          ) as MutableCollection<*>
          (currentValue as MutableCollection<*>).removeAll(deleteValue.toSet())
          currentValue
        } else null
      }
    } else {
      if (inheritData.relative?.containsKey(jsonFieldName) == true) {
        val relativeJsonValue = inheritData.relative.getValue(jsonFieldName)
        val relativeValue =
          parseJsonField(fieldType, relativeJsonValue, param)
        return if (currentValue != null) {
          if (currentValue is CddaSubObject && relativeValue is CddaSubObject) {
            currentValue.relative(relativeJsonValue, param)
            currentValue
          } else if (currentValue is Double && relativeValue is Double) {
            Double.relative(relativeJsonValue, currentValue)
          } else if (currentValue is Int && relativeValue is Int) {
            Int.relative(relativeJsonValue, currentValue)
          } else throw Exception("fieldClass(${fieldClass.jvmName}) not baseType or CddaSubObject")
        } else relativeValue
      }
      if (inheritData.proportional?.containsKey(jsonFieldName) == true) {
        return if (currentValue != null) {
          val proportionalJsonValue = inheritData.proportional.getValue(jsonFieldName)
          val proportionalValue =
            parseJsonField(fieldType, proportionalJsonValue, param)
          if (currentValue is CddaSubObject && proportionalValue is CddaSubObject) {
            currentValue.proportional(proportionalJsonValue, param)
            currentValue
          } else if (currentValue is Double && proportionalValue is Double) {
            Double.proportional(proportionalJsonValue, currentValue)
          } else if (currentValue is Int && proportionalValue is Int) {
            Int.proportional(proportionalJsonValue, currentValue)
          } else throw Exception("fieldClass(${fieldClass.jvmName}) not baseType or CddaSubObject")
        } else null
      }
    }
    return currentValue
  }

  private fun javaField2JsonField(fieldName: String): String {
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
    } else if (fieldClass.isSubclassOf(MutableMap::class)) {
      val keyType = fieldType.arguments[0].type!!
      val valueType = fieldType.arguments[1].type!!
      val fieldInstant: MutableMap<Any, Any> = mutableMapOf()
      if (jsonValue is JsonObject) {
        jsonValue.forEach {
          fieldInstant[parseJsonField(keyType, it.key, param)] = parseJsonField(valueType, it.value, param)
        }
      }
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


