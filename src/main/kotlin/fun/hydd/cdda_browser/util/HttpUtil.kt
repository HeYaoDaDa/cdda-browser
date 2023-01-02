package `fun`.hydd.cdda_browser.util

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.RequestOptions
import io.vertx.kotlin.coroutines.await

object HttpUtil {

  suspend fun request(vertx: Vertx, requestOptions: RequestOptions): Buffer? {
    return request(vertx, requestOptions, 0)
  }

  private suspend fun request(vertx: Vertx, requestOptions: RequestOptions, replyCount: Int): Buffer? {
    val client = vertx.createHttpClient()
    return try {
      client.request(requestOptions)
        .compose { obj: HttpClientRequest -> obj.send() }
        .compose { obj: HttpClientResponse -> obj.body() }
        .await()
    } catch (throwable: Throwable) {
      if (replyCount < 10) {
        request(vertx, requestOptions, replyCount + 1)
      } else {
        throw throwable
      }
    }
  }

}

