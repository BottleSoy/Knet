package top.soy_bottle.knet.protocols.knet

import top.soy_bottle.knet.config.SectionConfig
import top.soy_bottle.knet.protocols.ProtocolCreator

object KNetProtocolCreator : ProtocolCreator<KNetProtocol> {
	override fun createProtocol(config: SectionConfig): Result<KNetProtocol> = runCatching {
		KNetProtocol(config.getString("name"))
	}
}