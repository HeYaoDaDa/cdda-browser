package `fun`.hydd.cdda_browser.extension

import io.vertx.core.Future
import io.vertx.kotlin.coroutines.await
import java.util.concurrent.CompletionStage

fun <T> CompletionStage<T>.toFuture(): Future<T> {
  return Future.fromCompletionStage(this)
}

suspend fun <T> CompletionStage<T>.await(): T {
  return this.toFuture().await()
}
