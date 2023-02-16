package `fun`.hydd.cdda_browser.util.extension

import javax.persistence.criteria.Fetch
import javax.persistence.criteria.FetchParent
import javax.persistence.criteria.Path
import kotlin.reflect.KProperty1

fun <T, V> Path<T>.get(prop: KProperty1<T, V>): Path<V> = this.get(prop.name)
fun <T, V> FetchParent<*, T>.fetch(prop: KProperty1<T, V>): Fetch<T, V> = this.fetch(prop.name)
fun <T, V : Any> FetchParent<*, T>.fetchCollection(prop: KProperty1<T, Collection<V>>): Fetch<T, V> =
  this.fetch(prop.name)
