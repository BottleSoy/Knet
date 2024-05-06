package top.soy_bottle.knet.utils

import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.min


open class SizeLimitedInputStream(`in`: InputStream, size: Int = DEFAULT_BUFFER_SIZE) :
	BufferedInputStream(`in`, size), SizeLimitInput {
	
	/**
	 * 此流的读取的总字节数
	 */
	var totalSize: Long = 0
		protected set
	protected var limited = false
	protected var remaining = 0
	
	/**
	 * 重新填充缓冲区
	 */
	@Synchronized
	@Throws(IOException::class)
	override fun updateBuffer(read: Boolean): Boolean {
		val buffer = buf ?: throw IOException("Stream closed")
		val input = `in` ?: throw IOException("Stream closed")
		if (pos == 0 || markpos == 0) { //无需调整，只需要填充
			if (read && count < buffer.size) {
				val remainBytes = buffer.size - count
				if (remainBytes > 0) {
					val n = input.read(buffer, count, remainBytes)
					if (n > 0)
						count += n + pos
				}
			}
			return true
		} else if (markpos < 0) { //没有标记，移动count,pos
			val counts = count - pos
			System.arraycopy(buffer, pos, buffer, 0, counts)
			count -= pos
			pos = 0
			return updateBuffer(read)
		} else if (markpos > 0) { //有标记，移动count,pos,markpos
			val counts = count - markpos
			System.arraycopy(buffer, markpos, buffer, 0, counts)
			count -= markpos
			pos -= markpos
			markpos = 0
			return updateBuffer(read)
		} else {
			throw IOException("Invaild status")
		}
	}
	
	/**
	 * 重置缓冲区
	 */
	@Synchronized
	override fun reset() {
		remaining = marklimit
		super.reset()
	}
	
	/**
	 * 取消缓冲区大小限制
	 */
	@Synchronized
	override fun unlimit() {
		limited = false
	}
	
	/**
	 * 重置回目标点，
	 * 并且不带限制
	 */
	@Synchronized
	override fun resetUnlimited() {
		limited = false
		super.reset()
	}
	
	/**
	 * 限制读取长度
	 * 在达到限制长度之后再读取会报错
	 * 并且报错不会影响流的再次正常读取
	 * 可以通过reset重新读取
	 */
	@Synchronized
	override fun markLimited(readlimit: Int, read: Boolean) {
		updateBuffer(read)
		limited = true
		mark(readlimit)
	}
	
	/**
	 * 改变markLimit
	 */
	@Synchronized
	override fun requireMorelimit(readlimit: Int) {
		if (marklimit >= readlimit) {
			throw IOException("invaild new readLimit");
		}
		marklimit = readlimit
	}
	
	@Synchronized
	override fun mark(readlimit: Int) {
		remaining = readlimit
		super.mark(readlimit)
	}
	
	@Synchronized
	@Throws(IOException::class)
	override fun read(): Int {
		if (limited) {
			if (remaining == 0) {
				throw IOException("Read limit exceeded")
			} else {
				remaining--
			}
		}
		totalSize++
		return super.read()
	}
	
	@Synchronized
	@Throws(IOException::class)
	override fun read(b: ByteArray, off: Int, len: Int): Int {
		if (limited && remaining == 0) {
			throw IOException("Read limit exceeded")
		}
		val beforeTotal = totalSize
		val result =
			if (limited) { //限制读取空间
				
				/* 这个是在读取前获取的数量
				 * 外部原因有可能会导致部分stream调用`read(int)`方法
				 * 但是有部分stream不一定是这样的
				 * 所以要在这里记录读了多少，后面统一修改
				 */
				val beforeCount = remaining
				val newLen = min(len, remaining)
				val res = super.read(b, off, newLen)
				remaining = beforeCount - res
				res
			} else {
				super.read(b, off, len)
			}
		totalSize = beforeTotal + result
		return result
	}
	
	override fun getLimitedBuffer(size: Int): ByteArray {
		if (limited) {
			val b = ByteArray(size)
			System.arraycopy(buf, 0, b, 0, size)
			return b
		}
		throw IOException()
	}
	
	override fun toString(): String {
		return "SizeLimitedInputStream(" +
			"count=$count, " +
			"pos=$pos, " +
			"markpos=$markpos, " +
			"limited=$limited, " +
			"remaining=$remaining, " +
			"totalSize=$totalSize" +
			")"
	}
}

interface SizeLimitInput {
	/**
	 * 重新填充缓冲区
	 */
	@Throws(IOException::class)
	fun updateBuffer(read: Boolean = false): Boolean
	
	/**
	 * 重置缓冲区
	 */
	fun reset()
	
	/**
	 * 取消缓冲区大小限制
	 */
	fun unlimit()
	
	/**
	 * 重置回目标点，
	 * 并且不带限制
	 */
	fun resetUnlimited()
	
	/**
	 * 限制读取长度
	 * 在达到限制长度之后再读取会报错
	 * 并且报错不会影响流的再次正常读取
	 * 可以通过reset重新读取
	 */
	fun markLimited(readlimit: Int, read: Boolean = false)
	
	/**
	 * 改变markLimit
	 */
	fun requireMorelimit(readlimit: Int)
	
	fun mark(readlimit: Int)
	
	@Throws(IOException::class)
	fun read(): Int
	
	@Throws(IOException::class)
	fun read(b: ByteArray, off: Int, len: Int): Int
	fun getLimitedBuffer(size: Int): ByteArray
}

fun InputStream.limitedBuffer(bufferSize: Int = DEFAULT_BUFFER_SIZE) = when (this) {
	is SizeLimitedInputStream -> this
	else -> SizeLimitedInputStream(this, bufferSize)
}

/**
 * 创建一个
 */
inline fun <R> SizeLimitedInputStream.markWith(size: Int, function: SizeLimitedInputStream.() -> R): R {
	try {
		this.markLimited(size)
		return function()
	} finally {
		resetUnlimited()
	}
}

