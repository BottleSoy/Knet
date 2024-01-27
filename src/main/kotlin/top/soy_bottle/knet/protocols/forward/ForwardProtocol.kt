package top.soy_bottle.knet.protocols.forward

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection
import java.lang.IllegalStateException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketAddress

/**
 * 反向代理协议
 */
abstract class ForwardProtocol : AbstractProtocol<ForwardConnection> {
	
	override val connections = hashSetOf<ForwardConnection>()
	
	/**
	 * 反代一定是检测通过的！
	 */
	override fun detect(connection: Connection) = true
	
	@Throws(Exception::class)
	override fun directHandle(connection: Connection) {
		val con = selectForwardTarget(connection).forwardConnection(connection, this)
			?: throw IllegalStateException("Forward Connection is null")
		connections += con
	}
	
	abstract fun selectForwardTarget(connection: Connection): ForwardTarget
	
}