package top.soy_bottle.knet.utils

import kotlinx.coroutines.*
import top.soy_bottle.knet.logger
import java.io.InputStream
import java.io.OutputStream
import java.lang.IllegalStateException

/**
 * 流复制机，同时保存了读写量
 */
class Copier(
	private val i: InputStream,
	private val o: OutputStream,
	private val bufferSize: Int = DEFAULT_BUFFER_SIZE,
	val name: String = "default Copier",
	scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : CoroutineScope by scope {
	var count: Long = 0
		private set
	var started = false
		private set
	
	fun start(): Deferred<Result<Long>> {
		if (started)
			throw IllegalStateException("copier already started")
		started = true
		return this.async(Dispatchers.IO) {
			runCatching {
				val buffer = ByteArray(bufferSize)
				var bytes = i.read(buffer)
				while (bytes >= 0) {
					o.write(buffer, 0, bytes)
					o.flush()
					logger.info("[Copier] [$name] write $bytes bytes")
					count += bytes
					bytes = i.read(buffer)
				}
				count
			}
		}
	}
	
}