package top.soy_bottle.knet.mappers.udp

import top.soy_bottle.knet.client.KNetClient
import top.soy_bottle.knet.mappers.ClientMapper
import top.soy_bottle.knet.mappers.MapperType
import top.soy_bottle.knet.utils.writeString
import java.io.ByteArrayOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress

class ClientUdpMapper(
	val client: KNetClient,
	val config: UdpMapperConfig,
	id: Int,
) : ClientMapper("udp", id) {
	override val name: String = config.name
	
	override fun createClientMsg(): ByteArray = ByteArrayOutputStream()
		.writeString(config.forward ?: "")
		.toByteArray()
	
	
}