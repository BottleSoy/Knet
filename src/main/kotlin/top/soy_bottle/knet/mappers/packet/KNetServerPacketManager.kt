package top.soy_bottle.knet.mappers.packet

import top.soy_bottle.knet.mappers.tcp.*

class KNetServerPacketManager: PacketManager() {
	init {
		registerSendPacket<ServerPong>()
		registerSendPacket<ServerMapResult>()
		
		registerSendPacket<ServerNewTcpConnectionPacket>()
		registerSendPacket<ServerTcpDataPacket>()
		registerSendPacket<ServerTcpClosePacket>()
		
		registerRecivePacket(::ClientPing)
		registerRecivePacket(::ClientMappers)
		
		registerRecivePacket(::ClientTcpDataPacket)
		registerRecivePacket(::ClientTcpClosePacket)
	}

}