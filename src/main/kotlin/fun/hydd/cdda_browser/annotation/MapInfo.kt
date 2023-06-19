package `fun`.hydd.cdda_browser.annotation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class MapInfo(
  val key: String = "",
  val param: String = "",
  val ignore: Boolean = false,
  val spFun: String = ""
)
