package top.soy_bottle.knet.protocols.tcp

import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.socket.ConnectionManager
import top.soy_bottle.knet.socket.TcpServer
import top.soy_bottle.knet.socket.TcpServerConfig

class TcpProtocol(override val name: String, val config: TcpProtocolConfig) : AbstractProtocol<TcpConnection> {
	override val type: String = "tcp"
	override val connections = hashSetOf<TcpConnection>()
	
	val protocolSelector = config.selector
	
	val server = TcpServer(object : TcpServerConfig() {
		override val address = config.listen
		override val handler = ConnectionManager {
			val connection = TcpConnection(this@TcpProtocol, it)
			println(connection)
			connections += connection
			val res = protocolSelector.selectAndHandle(connection)
			
			if (res.isFailure) {
				connection.close()
			}
		}
	})
	
	init {
		val res = server.bindSync()
		res.fold({
			server.start()
		}, {
			it.printStackTrace()
		})
	}
	
	override fun detect(connection: Connection) = false
	
	override fun directHandle(connection: Connection) =
		throw IllegalAccessException("Tcp Protocol can not handle Connection")
}