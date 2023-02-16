package `fun`.hydd.cdda_browser.util.extension

import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
  handler { ctx ->
    CoroutineScope(ctx.vertx().dispatcher()).launch {
      try {
        fn(ctx)
        ctx.response()
      } catch (e: Exception) {
        ctx.fail(e)
      }
    }
  }
}

fun <T> Route.coroutineRespond(fn: suspend (RoutingContext) -> T) {
  handler { ctx ->
    CoroutineScope(ctx.vertx().dispatcher()).launch {
      try {
        ctx.json(fn(ctx))
      } catch (e: Exception) {
        ctx.fail(e)
      }
    }
  }
}
