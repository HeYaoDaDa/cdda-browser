package `fun`.hydd.cdda_browser.util

import io.vertx.kotlin.coroutines.awaitBlocking
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit


object ProcessUtil {
  private val log = LoggerFactory.getLogger(this.javaClass)

  private suspend fun execute(processBuilder: ProcessBuilder, timeout: Long) {
    processBuilder.inheritIO()
    awaitBlocking {
      val process = processBuilder.start();
      val result = process.waitFor(timeout, TimeUnit.MINUTES)
      process.destroy()
      if (!result) {
        val stringBuilder = StringBuilder()
        for (command in processBuilder.command()) {
          stringBuilder.append(command).append("\n")
        }
        log.error(
          "process execute fail, commands is $stringBuilder exit code is ${process.exitValue()}"
        )
      }
    }
  }

  suspend fun po2Json(poFilePath: String, outFilePath: String) {
    val poFile = File(poFilePath)
    val p = ProcessBuilder(
      "/usr/bin/po2json",
      poFilePath,
      outFilePath,
      "-d",
      "cataclysm-dda",
      "-f",
      "jed1.x"
    )
    p.directory(File(poFile.parent))
    execute(p,4)
  }
}
