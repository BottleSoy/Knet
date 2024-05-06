package top.soy_bottle.knet.protocols

import top.soy_bottle.knet.mappers.tcp.MappedTcpConnection

/**
 * 网络协议系统
 */
object ProtocolSystem {
	val protocols = hashMapOf<String, AbstractProtocol<*>>()
	
	fun <P : AbstractProtocol<*>> getProtocol(name: String) = protocols[name] as? P
	
	/**
	 * 添加一个协议
	 */
	operator fun plusAssign(newProtocol: AbstractProtocol<*>) {
		protocols[newProtocol.name] = newProtocol
	}
	
	operator fun minusAssign(protocol: AbstractProtocol<*>) {
		protocols.remove(protocol.name)
	}
	
}