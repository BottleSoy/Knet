package top.soy_bottle.knet.protocols.forward

import top.soy_bottle.knet.protocols.Connection

/**
 * 实现负载均衡的前进协议
 */
class LoadBlanaceForwardProtocol : ForwardProtocol() {
	
	override fun directHandle(connection: Connection) {
		TODO("Not yet implemented")
	}
	
	override fun selectForwardTarget(connection: Connection): ForwardTarget {
		TODO("Not yet implemented")
	}
	
	override val type: String
		get() = TODO("Not yet implemented")
	override val name: String
		get() = TODO("Not yet implemented")
	
}