package top.soy_bottle.knet.mappers.tcp

import org.slf4j.Logger
import top.soy_bottle.knet.logger
import top.soy_bottle.knet.mappers.TypeManager
import top.soy_bottle.knet.protocols.knet.KNetConnection
import top.soy_bottle.knet.utils.Env
import top.soy_bottle.knet.utils.Scope
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
@Env(Scope.Server)
class ServerTcpTypeManager(val connection: KNetConnection) : TypeManager {
	var idCount = 0
	val connections = hashMapOf<Int, ServerTcpConnection>()
	
	init {
		connection.packetManager.registerSendPacket<ServerNewTcpConnectionPacket>()
		connection.packetManager.registerSendPacket<ServerTcpDataPacket>()
		connection.packetManager.registerSendPacket<ServerTcpClosePacket>()
		
		
		connection.packetManager.registerRecivePacket(::ClientNewTcpConnectionPacket) {
			connections[it.connectionId]!!.start()
		}
		connection.packetManager.registerRecivePacket(::ClientTcpDataPacket) { packet ->
			val connection = connections[packet.connectionId]
			if (connection != null) {
				connection.send(packet.data, packet.len)
			} else {
				logger.warn("Connection ID:${packet.connectionId} already cloesd and ignore ${packet.len}")
			}
		}
		connection.packetManager.registerRecivePacket(::ClientTcpClosePacket) {
			connections.remove(it.connectionId)?.requestClose()
		}
	}
}