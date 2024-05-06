package top.soy_bottle.knet.utils.io

import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.Channel
import java.nio.channels.ReadableByteChannel

open class CloseFlag : Closeable, Channel {
	private var closed = false
	override fun close() {
		closed = true
	}
	
	override fun isOpen() = !closed
}

fun newInputChannel(inputHandler: CloseFlag.(ByteBuffer) -> Int): ReadableByteChannel =
	object : CloseFlag(), ReadableByteChannel {
		override fun read(dst: ByteBuffer): Int = inputHandler(this, dst)
	}

fun newInputStream(inputHandler: (data: ByteArray, off: Int, len: Int) -> Int): InputStream =
	object : InputStream() {
		val flag = CloseFlag()
		override fun close() {
			flag.close()
		}
		
		override fun read(data: ByteArray, off: Int, len: Int): Int {
			return inputHandler(data, off, len)
		}
		
		override fun read(): Int {
			if (flag.isOpen) {
				val data = ByteArray(1)
				val size = read(data)
				return if (size == 0) -1 else data[0].toUByte().toInt()
			} else {
				throw IOException("Stream closed")
			}
		}
		
	}