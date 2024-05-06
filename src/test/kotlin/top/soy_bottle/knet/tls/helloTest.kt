package top.soy_bottle.knet.tls

import top.soy_bottle.knet.utils.coroutine.BasicJob
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

fun main() {
	repeat(100) { it: Int ->
		BasicJob {
			println("start at $it")
			val res = runCatching {
				val client = Socket()
				client.connect(InetSocketAddress("127.0.0.1", 555))
				client.getInputStream().read(ByteArray(2048))
			}
		}
	}
	Thread.sleep(Long.MAX_VALUE)
}
