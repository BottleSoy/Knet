package top.soy_bottle.knet.mappers

import top.soy_bottle.knet.mappers.tcp.ServerTcpTypeManager
import top.soy_bottle.knet.mappers.udp.ServerUdpTypeManager
import top.soy_bottle.knet.protocols.knet.KNetConnection

class ServerMapperManager(connection: KNetConnection) {
	operator fun get(type: String) = managers[type]!!
	val managers = hashMapOf<String, TypeManager>()
	val types = connection.protocol.types
	val mappers = hashMapOf<Int, ServerMapper>()
	
	init {
		managers["tcp"] = ServerTcpTypeManager(connection)
		managers["udp"] = ServerUdpTypeManager(connection)
	}
}