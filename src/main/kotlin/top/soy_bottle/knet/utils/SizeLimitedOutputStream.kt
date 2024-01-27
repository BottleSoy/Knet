package top.soy_bottle.knet.utils

import top.soy_bottle.knet.logger
import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

open class SizeLimitedOutputStream(out: OutputStream, val size: Int = DEFAULT_BUFFER_SIZE) :
	FilterOutputStream(out) {
	protected var buf: ByteArray = ByteArray(size)
	
	/**
	 * The number of valid bytes in the buffer. This value is always
	 * in the range `0` through `buf.length`; elements
	 * `buf[0]` through `buf[count-1]` contain valid
	 * byte data.
	 */
	protected var count = 0
	protected var locked = false
	
	/**
	 * 锁定Stream，不能向目标流发送任何消息，
	 * 同时启用回退。
	 *
	 * `note:之前的消息都应该被发送`
	 */
	fun lockBuffer() {
		flush()
		locked = true
	}
	
	fun unlockBuffer() {
		locked = false
	}
	
	@Synchronized
	fun undo(len: Int) {
		if (!locked) {
			throw IOException("SizeLimitedOutputStream not locked!!!")
		}
		if (len > count) {
			throw IOException("len > count")
		}
		count -= len
	}
	
	@Synchronized
	override fun write(b: ByteArray, off: Int, len: Int) {
		if (size == 0) return out.write(b, off, len)
		if ((len >= buf.size || len > buf.size - count) && locked) {
			throw IOException("Write limit exceeded")
		}
		if (len >= buf.size) {
			flushBuffer()
			out.write(b, off, len)
			return
		}
		if (len > buf.size - count) {
			flushBuffer()
		}
		System.arraycopy(b, off, buf, count, len)
		count += len
	}
	
	@Synchronized
	override fun write(b: Int) {
		if (size == 0) return out.write(b)
		if (count >= buf.size && locked) {
			throw IOException("Write limit exceeded")
		}
		if (count >= buf.size) {
			flushBuffer()
		}
		buf[count++] = b.toByte()
	}
	@Synchronized
	@Throws(IOException::class)
	private fun flushBuffer() {
		if (count > 0) {
			out.write(buf, 0, count)
			out.flush()
			logger.info("[SizedLimitOutput] flush $count bytes to $out")
			count = 0
		}
	}
	
	@Synchronized
	override fun flush() {
		if (locked)
			throw IOException("SizeLimitedOutputStream locked")
		flushBuffer()
	}
	
	
	companion object {
		private const val DEFAULT_BUFFER_SIZE = 4096
	}
}

fun OutputStream.limitedBuffer(bufferSize: Int = DEFAULT_BUFFER_SIZE) = when (this) {
	is SizeLimitedOutputStream -> this
	else -> SizeLimitedOutputStream(this, bufferSize)
}
