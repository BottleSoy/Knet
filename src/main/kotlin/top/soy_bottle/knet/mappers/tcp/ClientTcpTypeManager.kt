package top.soy_bottle.knet.mappers.tcp

import top.soy_bottle.knet.client.KNetClient
import top.soy_bottle.knet.mappers.TypeManager

class ClientTcpTypeManager(val client: KNetClient) : TypeManager {
	val connections = hashMapOf<Int, ClientTcpConnection>()
	
	init {
		client.packetManager.registerSendPacket<ClientNewTcpConnectionPacket>()
		client.packetManager.registerSendPacket<ClientTcpDataPacket>()
		client.packetManager.registerSendPacket<ClientTcpClosePacket>()
		
		client.packetManager.registerRecivePacket(::ServerNewTcpConnectionPacket) {
			val client = (client.mappers[it.mapperId] as ClientTcpMapper).createNewTcpConnection(it)
			connections[it.connectionId] = client
		}
		client.packetManager.registerRecivePacket(::ServerTcpDataPacket) {
			connections[it.connectionId]?.sendData(it.data, it.len)
		}
		client.packetManager.registerRecivePacket(::ServerTcpClosePacket) {
			connections[it.connectionId]?.requestClose()
		}
		
	}
}