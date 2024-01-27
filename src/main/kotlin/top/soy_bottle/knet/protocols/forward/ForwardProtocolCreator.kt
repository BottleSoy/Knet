package top.soy_bottle.knet.protocols.forward

import top.soy_bottle.knet.config.SectionConfig
import top.soy_bottle.knet.protocols.ProtocolCreator

object ForwardProtocolCreator : ProtocolCreator<ForwardProtocol> {
	override fun createProtocol(config: SectionConfig) = runCatching {
		val forward = config.getString("forward")
		SimpleForwardProtocol(config.getString("name"), ForwardTarget.byString(forward))
	}
}