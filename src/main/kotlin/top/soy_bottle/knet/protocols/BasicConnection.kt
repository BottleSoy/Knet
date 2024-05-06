package top.soy_bottle.knet.protocols

import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.SizeLimitedOutputStream
import java.lang.StringBuilder
import java.net.Socket
import java.net.SocketAddress
import java.nio.channels.SocketChannel

abstract class BasicConnection(val connection: Connection) : Connection() {
	abstract override val protocol: AbstractProtocol<*>
	
	override val parents: List<Connection> = connection.parents + connection
	
	override val input: SizeLimitedInputStream get() = connection.input
	override val output: SizeLimitedOutputStream get() = connection.output
	
	override val remoteAddress: SocketAddress?
		get() = connection.remoteAddress
	override val localAddress: SocketAddress?
		get() = connection.localAddress
	
	override fun toString(): String =
		"${javaClass.simpleName}:${protocol.name}, " +
			"parents:${parents.map { it.toString() }.toList()}"
	
	override fun close() {
		super.close()
		parents.last().close()
	}
}