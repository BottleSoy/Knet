package top.soy_bottle.knet.protocols.haproxy

import top.soy_bottle.knet.protocols.selector.ProtocolSelector

class HAProxyProtocolConfig(
	val decodeV1: Boolean,
	val decodeV2: Boolean,
	val selector: ProtocolSelector
)
