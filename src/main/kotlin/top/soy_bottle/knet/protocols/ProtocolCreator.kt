package top.soy_bottle.knet.protocols

import top.soy_bottle.knet.config.SectionConfig

/**
 * 协议创建器
 *
 * [SectionConfig]->[Protocol]
 */
fun interface ProtocolCreator<Protocol : AbstractProtocol<*>> {
	
	fun createProtocol(config: SectionConfig): Result<Protocol>
}
