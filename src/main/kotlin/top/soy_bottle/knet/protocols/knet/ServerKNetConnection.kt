package top.soy_bottle.knet.protocols.knet

import top.soy_bottle.knet.mappers.ServerMapperManager
import top.soy_bottle.knet.mappers.packet.Packet
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.utils.Env
import top.soy_bottle.knet.utils.Scope

@Env(Scope.Server)
interface ServerKNetConnection {
	val mapperManager: ServerMapperManager
	fun sendPacket(packet: Packet)
	
	val connection: Connection
	
}