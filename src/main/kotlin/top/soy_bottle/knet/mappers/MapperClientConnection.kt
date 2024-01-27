package top.soy_bottle.knet.mappers

import top.soy_bottle.knet.mappers.packet.Packet
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.utils.Env
import top.soy_bottle.knet.utils.Scope

@Env(Scope.Server)
interface MapperClientConnection {
	val mappers: List<ServerMapper>
	
	fun sendPacket(packet: Packet)
	
	val connection: Connection
	
}