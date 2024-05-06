package top.soy_bottle.knet.socket

import top.soy_bottle.knet.logger
import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.SizeLimitedOutputStream
import top.soy_bottle.knet.utils.limitedBuffer
import java.net.*
import java.nio.channels.Channels
import java.nio.channels.SocketChannel

class TcpClient(
	val address: SocketAddress,
) {
	var connected = false
		private set
	val socket = SocketChannel.open(address.family)
	lateinit var input: SizeLimitedInputStream
	lateinit var output: SizeLimitedOutputStream
	fun start(): Result<Unit> {
		return runCatching {
			logger.info("{C->P} connecting to $address")
			val res = socket.connect(address)
			logger.info("{C->P} connecting res $res to $address")
			socket.configureBlocking(true)
			input = Channels.newInputStream(socket).limitedBuffer()
			output = Channels.newOutputStream(socket).limitedBuffer()
			connected = true
		}
	}
	
	
	fun close() {
		connected = false
		socket.close()
	}
}

val SocketAddress.family: StandardProtocolFamily
	get() {
		return when (this) {
			is InetSocketAddress -> {
				when (this.address) {
					is Inet4Address -> StandardProtocolFamily.INET
					is Inet6Address -> StandardProtocolFamily.INET6
				}
			}
			
			is UnixDomainSocketAddress -> StandardProtocolFamily.UNIX
			else -> throw IllegalAccessException()
		}
	}