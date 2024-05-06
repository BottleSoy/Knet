package top.soy_bottle.knet.mappers.tcp

import top.soy_bottle.knet.client.KNetClient
import top.soy_bottle.knet.config.SectionConfig
import top.soy_bottle.knet.config.getStringOrNull
import top.soy_bottle.knet.protocols.knet.ServerKNetConnection
import top.soy_bottle.knet.mappers.MapperType
import top.soy_bottle.knet.protocols.knet.KNetProtocol
import top.soy_bottle.knet.utils.Env
import top.soy_bottle.knet.utils.Scope
import top.soy_bottle.knet.utils.readString
@Env(Scope.Both)
object TcpMapperType : MapperType {
	override val name: String = "tcp"
	
	override fun createClientMapper(client: KNetClient, config: SectionConfig, id: Int): ClientTcpMapper {
		val c = TcpMapperConfig(
			config.getString("name"),
			config.getString("address"),
			config.getStringOrNull("forward"),
			config.getStringOrNull("haproxyProtocol"),
		)
		return ClientTcpMapper(client, c, id)
	}
	
	override fun createServerMapper(
		protocol: KNetProtocol,
		client: ServerKNetConnection,
		id: Int, msg: ByteArray,
	): ServerTcpMapper {
		val i = msg.inputStream()
		return ServerTcpMapper(protocol, client, id,name= i.readString(),forward= i.readString())
	}
}