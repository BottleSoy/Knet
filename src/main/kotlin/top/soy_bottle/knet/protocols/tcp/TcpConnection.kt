package top.soy_bottle.knet.protocols.tcp

import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.socket.TcpServerConnection
import java.net.SocketAddress

class TcpConnection(override val protocol: TcpProtocol, val c: TcpServerConnection) : Connection() {
	
	override val parents = emptyList<Connection>()
	
	
	override val input = c.input
	override val output = c.output
	
	override fun close() {
		super.close()
		c.server.clients.remove(c)
		c.socket.close()
	}
	
	override val remoteAddress: SocketAddress = c.socket.remoteAddress
	override val localAddress: SocketAddress = c.socket.localAddress
	override fun toString() = "TcpConnection:${protocol.name}-$remoteAddress"
	
}