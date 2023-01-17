package `fun`.hydd.cdda_browser.model.codec

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec


class UnitCodec : MessageCodec<Unit, Unit> {
  override fun encodeToWire(buffer: Buffer, s: Unit) {
  }

  override fun decodeFromWire(pos: Int, buffer: Buffer) {
    return
  }

  override fun name(): String {
    return "kotlin.Unit"
  }

  override fun systemCodecID(): Byte {
    return -1
  }

  override fun transform(s: Unit) {
    return s
  }
}
