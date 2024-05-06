package top.soy_bottle.knet.protocols.udp

import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.SizeLimitedOutputStream
import top.soy_bottle.knet.utils.io.newInputStream
import top.soy_bottle.knet.utils.limitedBuffer
import java.io.InputStream
import java.net.SocketAddress

class UdpConnection(
	override val protocol: UdpProtocol,
	val c: UdpServerConnection,
) : Connection() {
	override val parents: List<Connection> = emptyList()
	
	override val input: SizeLimitedInputStream = newInputStream { data: ByteArray, off: Int, len: Int ->
		TODO()
	}.limitedBuffer()
	override val output: SizeLimitedOutputStream = TODO()
	override val remoteAddress: SocketAddress = TODO()
	override val localAddress: SocketAddress = TODO()
	
	override fun toString(): String {
		TODO()
	}
}