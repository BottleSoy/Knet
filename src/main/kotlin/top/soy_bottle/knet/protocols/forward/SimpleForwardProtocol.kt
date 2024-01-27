package top.soy_bottle.knet.protocols.forward

import top.soy_bottle.knet.protocols.Connection
import java.net.SocketAddress

class SimpleForwardProtocol(override val name: String, val target: ForwardTarget) : ForwardProtocol() {
	override val type = "SimpleForward"
	
	override fun selectForwardTarget(connection: Connection) = target
}