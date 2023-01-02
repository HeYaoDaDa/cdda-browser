package `fun`.hydd.cdda_browser.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class StringUtilKtTest {
  @Test
  fun getStringHashTest() {
    Assertions.assertEquals(
      "7509e5bda0c762d2bac7f90d758b5b2263fa01ccbc542ab5e3df163be08e6ca9",
      StringUtil.getStringHash("hello world!")
    )
  }
}
