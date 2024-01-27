package top.soy_bottle.knet.mappers.tcp

import kotlinx.coroutines.launch
import top.soy_bottle.knet.address.AddressType
import top.soy_bottle.knet.address.toAddress
import top.soy_bottle.knet.mappers.ClientMapper
import top.soy_bottle.knet.client.KNetClient
import top.soy_bottle.knet.protocols.haproxy.*
import top.soy_bottle.knet.utils.writeString
import java.io.ByteArrayOutputStream

class ClientTcpConnectionMapper(val client: KNetClient, val config: TcpMapperConfig, id: Int) :
	ClientMapper("tcp", id) {
	override val name = config.name
	val address = config.address.toAddress()
	val forward = config.forward ?: ""
	val haproxyProtocol = config.haproxyProtocol?.let {
		HAProxyProtocolVersion.valueOf(it.uppercase())
	}
	
	
	val connections = hashMapOf<Int, ClientTcpConnection>()
	
	override fun createClientMsg(): ByteArray = ByteArrayOutputStream()
		.writeString(name)
		.writeString(forward)
		.toByteArray()
	
	fun sendPacket(packet: TcpClientPacket) {
	
	}
	
	private fun createNewTcpConnection(packet: ServerNewTcpConnectionPacket) {
		val connection = ClientTcpConnection(this, packet.connectionId) {
			if (haproxyProtocol != null) {
				it.sendData(HAProxyMessageEncoder.encode(packet.info.realAddress, packet.info.localAddress, haproxyProtocol))
			}
		}
		connections[packet.connectionId] = connection
	}
	
	fun onPacket(packet: TcpServerPacket) {
		when (packet) {
			is ServerNewTcpConnectionPacket -> createNewTcpConnection(packet)
			is ServerTcpDataPacket -> connections[packet.connectionId]?.sendData(packet.data)
			is ServerTcpClosePacket -> connections.remove(packet.connectionId)?.close()
			
		}
	}
}