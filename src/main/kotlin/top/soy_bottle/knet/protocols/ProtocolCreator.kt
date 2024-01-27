package top.soy_bottle.knet.protocols

import top.soy_bottle.knet.config.SectionConfig

/**
 * [SectionConfig]->[Protocol]
 */
fun interface ProtocolCreator<Protocol : AbstractProtocol<*>> {
	
	fun createProtocol(config: SectionConfig): Result<Protocol>
}
