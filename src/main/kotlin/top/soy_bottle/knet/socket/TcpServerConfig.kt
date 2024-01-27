package top.soy_bottle.knet.socket

import java.net.SocketAddress
import javax.net.ServerSocketFactory

abstract class TcpServerConfig<Connection : AbstractTcpServerConnection> {
	abstract val address: SocketAddress
	abstract val handler: ConnectionManager<Connection>
	open val maxConnection: Int = Int.MAX_VALUE
	open val readSize: Int = 1024
	open val writeSize: Int = 1024
}