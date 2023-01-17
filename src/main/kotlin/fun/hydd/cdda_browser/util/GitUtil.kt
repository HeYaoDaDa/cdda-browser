package `fun`.hydd.cdda_browser.util

import `fun`.hydd.cdda_browser.constant.EventBusConstant
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.kotlin.coroutines.await
import org.eclipse.jgit.lib.Ref
import org.eclipse.jgit.revwalk.RevObject

/**
 * utils for handle git repo
 */
object GitUtil {

  /**
   * update git repo
   *
   * @param eventBus
   */
  suspend fun update(eventBus: EventBus) {
    eventBus.request<Unit>(EventBusConstant.GIT_REPO_UPDATE, null, DeliveryOptions().setSendTimeout(60_000)).await()
  }

  /**
   * rest git repo to tag
   *
   * @param eventBus
   * @param tagName rest to tag name
   */
  suspend fun hardRestToTag(eventBus: EventBus, tagName: String) {
    eventBus.request<Unit>(EventBusConstant.GIT_REPO_HARD_REST_TO_TAG, tagName).await()
  }

  /**
   * Ref convert to GitTagDto
   */
  suspend fun getRevObject(eventBus: EventBus, tagRef: Ref): RevObject {
    return eventBus.request<RevObject>(EventBusConstant.GIT_REPO_GET_REV_OBJECT, tagRef).await().body()
  }

  /**
   * return repo latest tagRef
   */
  suspend fun getLatestRevObject(eventBus: EventBus): RevObject? {
    return eventBus.request<RevObject?>(EventBusConstant.GIT_REPO_GET_LATEST_REV_OBJECT, null).await().body()
  }

  suspend fun getTagList(eventBus: EventBus): List<Ref> {
    return eventBus.request<List<Ref>>(EventBusConstant.GIT_REPO_GET_TAG_LIST, null).await().body()
  }
}
