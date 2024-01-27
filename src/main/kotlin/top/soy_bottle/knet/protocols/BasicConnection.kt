package top.soy_bottle.knet.protocols

import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.SizeLimitedOutputStream
import java.lang.StringBuilder
import java.net.Socket

abstract class BasicConnection(val connection: Connection) : Connection() {
	abstract override val protocol: AbstractProtocol<*>
	
	override val parents: List<Connection> = connection.parents + connection
	
	override val input: SizeLimitedInputStream get() = connection.input
	override val output: SizeLimitedOutputStream get() = connection.output
	
	private val jSocket: Socket get() = connection.javaSocket()
	
	override fun javaSocket(): Socket = jSocket
	
	override fun toString(): String =
		"${javaClass.simpleName}:${protocol.name}, " +
			"parents:${parents.map { it.protocol.name }.toList()}"
	
	override fun close() {
		super.close()
		parents.last().close()
	}
}