package `fun`.hydd.cdda_browser

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class LogTest {
  private val log = LoggerFactory.getLogger(this.javaClass)

  @Test
  fun logTest() {
    log.trace("trace")
    log.debug("debug")
    log.info("info")
    log.warn("warn")
    log.error("error")
  }
}
