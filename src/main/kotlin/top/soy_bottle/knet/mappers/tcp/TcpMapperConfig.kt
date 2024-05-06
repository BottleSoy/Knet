package top.soy_bottle.knet.mappers.tcp

import top.soy_bottle.knet.mappers.BasicMapperConfig
import top.soy_bottle.knet.utils.Env
import top.soy_bottle.knet.utils.Scope

@Env(Scope.Client)
class TcpMapperConfig(
	override val name: String,
	
	val address: String,
	val forward: String?,
	val haproxyProtocol: String?
) : BasicMapperConfig()