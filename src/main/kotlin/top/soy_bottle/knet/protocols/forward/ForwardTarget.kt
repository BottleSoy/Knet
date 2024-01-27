package top.soy_bottle.knet.protocols.forward

import top.soy_bottle.knet.address.toAddress
import top.soy_bottle.knet.protocols.Connection
import java.net.SocketAddress

/**
 *
 */
sealed class ForwardTarget(
	val type: ForwardTragetType,
) {
	abstract fun forwardConnection(connection: Connection, protocol: ForwardProtocol): ForwardConnection?
	
	companion object {
		fun byString(forward: String): ForwardTarget {
			return runCatching {
				if (forward.startsWith("udp:")) {
					return UDPForwardTarget(forward.substring(4).toAddress())
				} else if (forward.startsWith("tcp:")) {
					return TcpForwardTarget(forward.substring(4).toAddress())
				}
				TcpForwardTarget(forward.toAddress())
			}.getOrDefault(NoForwardTarget)
			
		}
	}
}

class TcpForwardTarget(val address: SocketAddress) : ForwardTarget(ForwardTragetType.ADDRESS_TCP) {
	override fun forwardConnection(connection: Connection, protocol: ForwardProtocol) =
		TcpForwardConnection(protocol, connection, this).start()
}

class UDPForwardTarget(val address: SocketAddress) : ForwardTarget(ForwardTragetType.ADDRESS_UDP) {
	override fun forwardConnection(connection: Connection, protocol: ForwardProtocol) =
		UdpForwardConnection(protocol, connection, this).start()
}

object NoForwardTarget : ForwardTarget(ForwardTragetType.NONE) {
	override fun forwardConnection(connection: Connection, protocol: ForwardProtocol) = null
}


enum class ForwardTragetType {
	ADDRESS_TCP,
	ADDRESS_UDP,
	NONE;
}


