package top.soy_bottle.knet.protocols.haproxy

import top.soy_bottle.knet.config.SectionConfig
import top.soy_bottle.knet.config.getOrDefault
import top.soy_bottle.knet.protocols.ProtocolCreator
import top.soy_bottle.knet.protocols.selector.EmptyProtocolSelector
import top.soy_bottle.knet.protocols.selector.ProtocolSelector

object HAProxyMessageProtocolCreator : ProtocolCreator<HAProxyMessageProtocol> {
	override fun createProtocol(config: SectionConfig): Result<HAProxyMessageProtocol> {
		return runCatching {
			val name = config.getString("name")
			val c = HAProxyProtocolConfig(
				config.getOrDefault("decodeV1", true),
				config.getOrDefault("decodeV2", true),
				ProtocolSelector.of(config["selector"]) ?: EmptyProtocolSelector
			)
			HAProxyMessageProtocol(name, c)
		}
	}
}