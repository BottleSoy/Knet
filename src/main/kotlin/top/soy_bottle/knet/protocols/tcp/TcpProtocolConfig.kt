package top.soy_bottle.knet.protocols.tcp

import top.soy_bottle.knet.protocols.selector.ProtocolSelector
import java.net.SocketAddress

data class TcpProtocolConfig(
	val listen: SocketAddress,
	val selector: ProtocolSelector
)