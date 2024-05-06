package top.soy_bottle.knet.mappers.udp

import top.soy_bottle.knet.client.KNetClient
import top.soy_bottle.knet.config.SectionConfig
import top.soy_bottle.knet.config.getOrDefault
import top.soy_bottle.knet.config.getStringOrNull
import top.soy_bottle.knet.mappers.ClientMapper
import top.soy_bottle.knet.mappers.MapperType
import top.soy_bottle.knet.mappers.ServerMapper
import top.soy_bottle.knet.protocols.knet.KNetProtocol
import top.soy_bottle.knet.protocols.knet.ServerKNetConnection
import top.soy_bottle.knet.utils.readString

object UdpMapperType : MapperType {
	override val name = "udp"
	
	override fun createClientMapper(
		client: KNetClient,
		config: SectionConfig,
		id: Int,
	): ClientMapper {
		val c = UdpMapperConfig(
			config.getString("name"),
			config.getStringOrNull("forward"),
			config.getOrDefault("timeout", 30 * 1000)
		)
		return ClientUdpMapper(client, c, id)
	}
	
	override fun createServerMapper(
		protocol: KNetProtocol,
		client: ServerKNetConnection,
		id: Int, msg: ByteArray,
	): ServerMapper {
		val i = msg.inputStream()
		return ServerUdpMapper(protocol, client, id, i.readString(), i.readString())
	}
	
}