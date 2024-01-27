package top.soy_bottle.knet.mappers.tcp

import top.soy_bottle.knet.mappers.BasicMapperConfig

class TcpMapperConfig(
	override val name: String,
	
	val address: String,
	val forward: String?,
	val haproxyProtocol: String?
) : BasicMapperConfig()