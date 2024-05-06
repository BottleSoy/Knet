package top.soy_bottle.knet.mappers.udp

import top.soy_bottle.knet.mappers.BasicMapperConfig

class UdpMapperConfig(
	override val name: String,
	val forward: String?,
	val timeout: Int = 30 * 1000,
	
) : BasicMapperConfig()