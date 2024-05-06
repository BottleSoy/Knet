package top.soy_bottle.knet.protocols.forward


import top.soy_bottle.knet.logger
import top.soy_bottle.knet.protocols.BasicConnection
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.socket.TcpClient
import top.soy_bottle.knet.utils.Copier
import top.soy_bottle.knet.utils.coroutine.BasicJob

/**
 * 反向代理连接
 */
sealed class ForwardConnection(
	override val protocol: ForwardProtocol,
	connection: Connection,
) : BasicConnection(connection) {
	abstract fun start(): ForwardConnection
}


class TcpForwardConnection(
	protocol: ForwardProtocol,
	connection: Connection,
	val target: TcpForwardTarget,
) : ForwardConnection(protocol, connection) {
	
	val client: TcpClient = TcpClient(target.address)
	lateinit var iCopier: Copier
	lateinit var oCopier: Copier
	
	override fun start(): TcpForwardConnection {
		BasicJob {
			val res = client.start()
			if (res.isSuccess) {
				logger.debug("[Forward] [Tcp] 目标${target.address.toString()}已连接")
				iCopier = Copier(input, client.output, name = "$tunnel UpStream")
				oCopier = Copier(client.input, output, name = "$tunnel DownStream")
				BasicJob {
					val resi = iCopier.start().await()
					logger.debug("[Forward] [Tcp] $tunnel 断开连接(UpStream)")
					close()
				}
				BasicJob {
					val reso = oCopier.start().await()
					logger.debug("[Forward] [Tcp] $tunnel 断开连接(DownStream)")
					close()
				}
			} else {
				close()
			}
		}
		return this
	}
	
	private val tunnel = "${connection.remoteAddress}->${target.address}"
	override fun close() {
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