package top.soy_bottle.knet.protocols.tcp

import kotlinx.coroutines.runBlocking
import top.soy_bottle.knet.logger
import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.protocols.ProtocolSystem
import top.soy_bottle.knet.socket.ConnectionManager
import top.soy_bottle.knet.socket.TcpServer
import top.soy_bottle.knet.socket.TcpServerConfig
import top.soy_bottle.knet.socket.TcpServerConnection

class TcpProtocol(override val name: String, val config: TcpProtocolConfig) : AbstractProtocol<TcpConnection> {
	override val type: String = "tcp"
	override val connections = hashSetOf<TcpConnection>()
	
	val protocolSelector = config.selector
	
	val server = TcpServer(object : TcpServerConfig<TcpServerConnection>() {
		override val address = config.listen
		override val handler = ConnectionManager<TcpServerConnection> {
			val connection = TcpConnection(this@TcpProtocol, it)
			connections += connection
			val res = protocolSelector.selectAndHandle(connection)
			
			if (res.isFailure) {
				connection.close()
			}
		}
	})
	
	init {
		runBlocking {
			server.bindSync()
			server.start()
		}
	}
	
	override fun detect(connection: Connection) = false
	
	override fun directHandle(connection: Connection) =
		throw IllegalAccessException("Tcp Protocol can not handle Connection")
}