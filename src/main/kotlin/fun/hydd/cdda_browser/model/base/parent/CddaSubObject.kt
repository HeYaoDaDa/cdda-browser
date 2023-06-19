package `fun`.hydd.cdda_browser.model.base.parent

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.util.JsonUtil
import io.vertx.core.json.JsonObject
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.full.functions
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.memberProperties

abstract class CddaSubObject {

  fun parse(jsonValue: Any, param: String) {
    if (jsonValue is JsonObject)
      this.autoMap(jsonValue)
    this.finalize(jsonValue, param)
  }

  open fun finalize(jsonValue: Any, param: String) {
    return
  }

  open fun relative(relativeJson: Any, param: String) {
    throw UnsupportedOperationException()
  }

  open fun proportional(proportionalJson: Any, param: String) {
    throw UnsupportedOperationException()
  }

  fun autoMap(jsonValue: Any) {
    this::class.memberProperties.filterIsInstance<KMutableProperty<*>>().forEach { prop ->
      val mapInfo = prop.findAnnotations(MapInfo::class).firstOrNull() ?: MapInfo()
      if (!mapInfo.ignore && jsonValue is JsonObject) {
        val jsonFieldName = mapInfo.key.ifBlank { JsonUtil.javaField2JsonField(prop.name) }
        if (jsonValue.containsKey(jsonFieldName)) {
          val subJsonValue = jsonValue.getValue(jsonFieldName)
          if (subJsonValue == null)
            prop.setter.call(this, null)
          else
            prop.setter.call(this, JsonUtil.parseJsonField(prop.returnType, subJsonValue, mapInfo.param))
        }
      }
      if (mapInfo.spFun.isNotBlank()) {
        val spFun = this::class.functions.firstOrNull() { it.name == mapInfo.spFun }
          ?: throw Exception("class ${this::class} spFun ${mapInfo.spFun} is miss")
        val args: MutableMap<KParameter, Any?> = mutableMapOf(spFun.instanceParameter!! to this)
        if (spFun.parameters.size == 2) args[spFun.parameters[1]] = jsonValue
        spFun.callBy(args)
      }
    }
  }
}
