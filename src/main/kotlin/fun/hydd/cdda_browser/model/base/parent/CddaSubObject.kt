package `fun`.hydd.cdda_browser.model.base.parent

import com.fasterxml.jackson.annotation.JsonInclude
import `fun`.hydd.cdda_browser.util.JsonUtil

@JsonInclude(JsonInclude.Include.NON_NULL)
abstract class CddaSubObject {

  fun parse(jsonValue: Any, param: String) {
    JsonUtil.autoLoad(this, jsonValue, null)
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
}
