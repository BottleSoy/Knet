package top.soy_bottle.knet.socket

import kotlinx.coroutines.*
import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.SizeLimitedOutputStream
import top.soy_bottle.knet.utils.limitedBuffer
import java.net.Socket
import java.net.SocketAddress

class TcpClient(
	val address: SocketAddress,
	scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : CoroutineScope by scope {
	var connected = false
		private set
	val socket = BufferedSocket()
	lateinit var `in`: SizeLimitedInputStream
	lateinit var `out`: SizeLimitedOutputStream
	fun start() = this.async(Dispatchers.IO) {
		runCatching {
			socket.connect(address)
			`in` = socket.getInputStream()
			`out` = socket.getOutputStream()
			connected = true
		}
	}
	
	
	fun close() {
		connected = false
		cancel("close")
		socket.close()
	}
}