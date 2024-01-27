package top.soy_bottle.knet.protocols.knet

import kotlinx.coroutines.launch
import top.soy_bottle.knet.mappers.MapperClientConnection
import top.soy_bottle.knet.mappers.ServerMapper
import top.soy_bottle.knet.mappers.packet.KNetServerPacketManager
import top.soy_bottle.knet.mappers.packet.Packet
import top.soy_bottle.knet.mappers.packet.PacketManager
import top.soy_bottle.knet.protocols.BasicConnection
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.utils.readString
import top.soy_bottle.knet.utils.readVarInt

class KNetConnection(override val protocol: KNetProtocol, connection: Connection) :
	BasicConnection(connection), MapperClientConnection {
	val packetManager = KNetServerPacketManager()
	val readerJob = protocol.launch {
		if (input.readString(4) != "KNET") return@launch close()
		while (true) {
			val packet = packetManager.readPacket(input)
		}
	}
	override val mappers: List<ServerMapper> = arrayListOf()
	
	override fun sendPacket(packet: Packet) {
		synchronized(output) {
			packetManager.writePacket(output, packet)
		}
	}
	
	override fun close() {
		super.close()
	}
}