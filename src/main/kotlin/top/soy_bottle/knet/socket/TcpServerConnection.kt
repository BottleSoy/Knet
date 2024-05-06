package top.soy_bottle.knet.socket

import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.SizeLimitedOutputStream
import top.soy_bottle.knet.utils.limitedBuffer
import java.nio.channels.Channels
import java.nio.channels.SocketChannel

class TcpServerConnection(
	val server: TcpServer,
	val socket: SocketChannel,
) {
	val attributes: HashMap<String, Any> = hashMapOf()
	val output = Channels.newOutputStream(socket).limitedBuffer(server.config.writeSize)
	val input = Channels.newInputStream(socket).limitedBuffer(server.config.readSize)
	fun close() {
		socket.close()
	}
}