package top.soy_bottle.knet.mappers.tcp

import top.soy_bottle.knet.address.toAddress
import top.soy_bottle.knet.mappers.ClientMapper
import top.soy_bottle.knet.client.KNetClient
import top.soy_bottle.knet.logger
import top.soy_bottle.knet.protocols.haproxy.*
import top.soy_bottle.knet.utils.writeString
import java.io.ByteArrayOutputStream

class ClientTcpMapper(val client: KNetClient, val config: TcpMapperConfig, id: Int) :
	ClientMapper("tcp", id) {
	override val name = config.name
	val address = config.address.toAddress()
	val forward = config.forward ?: ""
	val haproxyProtocol = config.haproxyProtocol?.let {
		HAProxyProtocolVersion.valueOf(it.uppercase())
	}
	
	override fun createClientMsg(): ByteArray = ByteArrayOutputStream()
		.writeString(name)
		.writeString(forward)
		.toByteArray()
	
	fun sendPacket(packet: TcpClientPacket) {
		client.packetManager.sendPacket(packet)
	}
	
	fun createNewTcpConnection(packet: ServerNewTcpConnectionPacket): ClientTcpConnection {
		logger.info("New tcp connection from: ${packet.info.realAddress}")
		val connection = ClientTcpConnection(this, packet.connectionId) {
			sendPacket(ClientNewTcpConnectionPacket(it.id))
			if (haproxyProtocol != null) {
				it.sendData(HAProxyMessageEncoder.encode(packet.info.realAddress, packet.info.localAddress, haproxyProtocol))
			}
		}
		return connection
	}
}