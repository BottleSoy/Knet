package top.soy_bottle.knet.socket

import java.net.SocketAddress
import javax.net.ServerSocketFactory

abstract class TcpServerConfig {
	abstract val address: SocketAddress
	abstract val handler: ConnectionManager
	open val maxConnection: Int = Int.MAX_VALUE
	open val readSize: Int = DEFAULT_BUFFER_SIZE
	open val writeSize: Int = DEFAULT_BUFFER_SIZE
}