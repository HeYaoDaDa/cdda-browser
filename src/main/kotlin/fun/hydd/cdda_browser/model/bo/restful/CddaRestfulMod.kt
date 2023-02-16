package `fun`.hydd.cdda_browser.model.bo.restful

data class CddaRestfulMod(
  val id: String,
  val name: String,
  val description: String,
  val obsolete: Boolean,
  val core: Boolean,
  val depModIds: Collection<String>,
  val allDepModIds: Collection<String>
)
