package top.soy_bottle.knet.protocols

/**
 * 网络协议系统
 */
object ProtocolSystem {
	val protocols = hashMapOf<String, AbstractProtocol<*>>()
	
	fun <P : AbstractProtocol<*>> getProtocol(name: String) = protocols[name] as? P
	
	operator fun plusAssign(newProtocol: AbstractProtocol<*>) {
		protocols[newProtocol.name] = newProtocol
	}
	
}