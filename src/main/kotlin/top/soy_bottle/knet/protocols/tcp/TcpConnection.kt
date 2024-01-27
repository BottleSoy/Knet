package top.soy_bottle.knet.protocols.tcp

import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.socket.TcpServerConnection

class TcpConnection(override val protocol: TcpProtocol, val c: TcpServerConnection) : Connection() {
	
	override val parents = emptyList<Connection>()
	override fun javaSocket() = c.socket
	
	override val input = c.socket.inputStream
	override val output = c.socket.outputStream
	
	override fun close() {
		super.close()
		c.server.clients.remove(c)
		c.socket.close()
	}
	
	override fun toString() = "TcpConnection:${protocol.name}"
	
}