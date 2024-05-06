package top.soy_bottle.knet.protocols.kcp

import top.soy_bottle.knet.protocols.BasicConnection
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.utils.*
import top.soy_bottle.knet.utils.io.newInputStream
import top.soy_bottle.knet.utils.io.newOutputStream
import top.soy_bottle.knet.utils.kcp.KCP


class KcpConnection(connection: Connection, override val protocol: KcpProtocol) : BasicConnection(connection) {
	private val kcp = object : KCP(1024) {
		override fun output(buffer: ByteArray, size: Int) {
			connection.output.writeByteArray(buffer, size)
			connection.output.flush()
		}
	}
	override val input: SizeLimitedInputStream = newInputStream { buffer, off, len ->
		kcp.Recv(buffer, off, len)
	}.limitedBuffer()
	override val output: SizeLimitedOutputStream = newOutputStream { b: ByteArray, off: Int, len: Int ->
		val newBytes = b.subArray(off, len)
		kcp.Send(newBytes)
	}.limitedBuffer()
}


