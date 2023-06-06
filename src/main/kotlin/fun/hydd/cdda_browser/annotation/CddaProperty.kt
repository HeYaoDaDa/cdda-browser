package `fun`.hydd.cdda_browser.annotation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class CddaProperty(val key: String = "", val para: String = "")
