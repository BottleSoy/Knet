package top.soy_bottle.knet.protocols

import top.soy_bottle.knet.config.SectionConfig
import top.soy_bottle.knet.protocols.forward.ForwardProtocol
import top.soy_bottle.knet.protocols.forward.ForwardProtocolCreator
import top.soy_bottle.knet.protocols.knet.KNetProtocol
import top.soy_bottle.knet.protocols.knet.KNetProtocolCreator
import top.soy_bottle.knet.protocols.tcp.TcpProtocolCreator
import top.soy_bottle.knet.protocols.tls.TLSProtocolCreator
import java.lang.RuntimeException

/**
 * 网络协议工厂，作用是集合所有的[ProtocolCreator]
 */
object ProtocolFactory {
	private val protocolMap = linkedMapOf<String, ProtocolCreator<*>>()
	
	fun getProtocols() = protocolMap.keys.toList()
	
	fun createProtocol(config: SectionConfig): AbstractProtocol<*> {
		val type = config.getString("type").lowercase()
		val creator = protocolMap[type] ?: throw RuntimeException("missing protocol type of:$type")
		return creator.createProtocol(config).getOrThrow()
	}
	
	init {
		protocolMap["tcp"] = TcpProtocolCreator
		protocolMap["tls"] = TLSProtocolCreator
		protocolMap["forward"] = ForwardProtocolCreator
		protocolMap["knet"] = KNetProtocolCreator
//		protocolMap["kcp"] =
		
	}
}