package `fun`.hydd.cdda_browser.model.cddaItem.cddaSubObject

import `fun`.hydd.cdda_browser.annotation.MapInfo
import `fun`.hydd.cdda_browser.model.base.parent.CddaSubObject
import `fun`.hydd.cdda_browser.model.entity.TranslationEntity
import io.vertx.core.json.JsonObject
import java.lang.instrument.IllegalClassFormatException

data class Translation(
  @MapInfo(ignore = true) var value: String = "",

  @MapInfo(ignore = true) var ctxt: String? = null
) : CddaSubObject() {

  override fun finalize(jsonValue: Any, param: String) {
    when (jsonValue) {
      is String -> {
        this.value = jsonValue
        this.ctxt = param.ifBlank { null }
      }

      is JsonObject -> {
        val newTranslation = of(jsonValue, param.ifBlank { null })
        this.value = newTranslation.value
        this.ctxt = newTranslation.ctxt
      }

      else -> throw IllegalArgumentException("value is $jsonValue, type is ${jsonValue::class}")
    }
  }

  companion object {
    fun of(translationEntity: TranslationEntity): Translation {
      return Translation(translationEntity.value!!, translationEntity.ctxt)
    }

    fun of(jsonObject: JsonObject, ctxt: String? = null): Translation {
      var lastCtxt = ctxt
      var isMale = false
      var isFemale = false
      val value: String
      if (jsonObject.containsKey("str")) {
        value = jsonObject.getString("str")
      } else if (jsonObject.containsKey("str_sp")) {
        value = jsonObject.getString("str_sp")
      } else if (jsonObject.containsKey("male")) {
        value = jsonObject.getString("male")
        isMale = true
      } else if (jsonObject.containsKey("female")) {
        value = jsonObject.getString("female")
        isFemale = true
      } else {
        throw IllegalClassFormatException("GettextString input json \n$jsonObject\nno have str, str_sp, male or female!")
      }
      lastCtxt = getLastCtxtByJsonObject(jsonObject, lastCtxt, isMale, isFemale)
      return Translation(value, lastCtxt)
    }

    private fun getLastCtxtByJsonObject(
      input: JsonObject, lastCtxt: String?, isMale: Boolean, isFemale: Boolean
    ): String? {
      var lastCtxt1 = lastCtxt
      if (input.containsKey("ctxt")) {
        val jsonCtxt = input.getString("ctxt")
        if (lastCtxt1 != null) throw IllegalArgumentException("input ctxt $lastCtxt1, but has json ctxt $jsonCtxt")
        else lastCtxt1 = jsonCtxt
      }
      if (lastCtxt1 != null) {
        if (isMale) lastCtxt1 += "_male"
        if (isFemale) lastCtxt1 += "_female"
      }
      return lastCtxt1
    }
  }
}
