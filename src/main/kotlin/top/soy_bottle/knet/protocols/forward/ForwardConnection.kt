package top.soy_bottle.knet.protocols.forward

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import top.soy_bottle.knet.logger
import top.soy_bottle.knet.protocols.BasicConnection
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.socket.TcpClient
import top.soy_bottle.knet.utils.Copier

/**
 * 反向代理连接
 */
sealed class ForwardConnection(
	override val protocol: ForwardProtocol,
	connection: Connection,
) : BasicConnection(connection), CoroutineScope by CoroutineScope(Dispatchers.IO) {
	abstract fun start(): ForwardConnection
}


class TcpForwardConnection(
	protocol: ForwardProtocol,
	connection: Connection,
	val target: TcpForwardTarget,
) : ForwardConnection(protocol, connection) {
	
	val client: TcpClient = TcpClient(target.address, this)
	lateinit var iCopier: Copier
	lateinit var oCopier: Copier
	
	override fun start(): TcpForwardConnection {
		launch {
			val res = client.start().await()
			if (res.isSuccess) {
				logger.info("[Forward] [Tcp] 目标${target.address}已连接")
				iCopier = Copier(input, client.out, name = "$tunnel UpStream", scope = this)
				oCopier = Copier(client.`in`, output, name = "$tunnel DownStream", scope = this)
				launch {
					val resi = iCopier.start().await()
					logger.info("[Forward] [Tcp] $tunnel 断开连接(iCopier)")
					close()
				}
				launch {
					val reso = oCopier.start().await()
					logger.info("[Forward] [Tcp] $tunnel 断开连接(oCopier)")
					close()
				}
			} else {
				close()
			}
			
		}
		return this
	}
	
	private val tunnel = "${connection.javaSocket().remoteSocketAddress}->${target.address}"
	override fun close() {
		this.cancel("connection close")
		super.close()
		client.close()
	}
}

/**
 * TODO: UDP代理连接
 */
class UdpForwardConnection(
	protocol: ForwardProtocol,
	connection: Connection,
	val target: UDPForwardTarget,
) : ForwardConnection(protocol, connection) {
	override fun start(): ForwardConnection {
		TODO()
	}
	
	override fun close() {
	
	}
	
}