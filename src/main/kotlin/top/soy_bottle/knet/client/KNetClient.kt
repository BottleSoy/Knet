package top.soy_bottle.knet.client

import top.soy_bottle.knet.KApplication
import top.soy_bottle.knet.config.*
import top.soy_bottle.knet.logger
import top.soy_bottle.knet.mappers.TypeManager
import top.soy_bottle.knet.mappers.packet.KNetClientPacketManager
import top.soy_bottle.knet.mappers.tcp.ClientTcpTypeManager
import top.soy_bottle.knet.mappers.udp.ClientUdpTypeManger
import kotlin.random.Random

class KNetClient(val config: KNetConfig) : KApplication {
	
	lateinit var connection: KNetClientConnection
	var packetManager: KNetClientPacketManager = KNetClientPacketManager(this)
	val mappers: MapperService = MapperService(this)
	val types = hashMapOf<String, TypeManager>()
	override fun start() {
		logger.info("客户端启动中...")
		when (config) {
			is JSConfig -> {
				config.getList("mappers").sectionList().forEachIndexed { index, it ->
					val mapper = mappers.mapperTypes[it.getString("type")]!!
						.createClientMapper(this, it, index)
					mappers.mappers += mapper
				}
				config.getSub("connection").let { section ->
					val config = ClientConnectionConfig(
						section.getOrDefault("name", randomString(32)),
						section.getString("address"),
						section.getString("token"),
						section.getSubOrNull("tls")?.let { tls ->
							ClientConnectionConfig.Tls(
								tls.getStringOrNull("serverName") ?: section.getString("address")
							)
						}
					)
					connection = KNetClientConnection(this, config)
					connection.start()
				
				}
			}
			
			is KTSConfig -> config.execute()
		}
		types["tcp"] = ClientTcpTypeManager(this)
		types["udp"] = ClientUdpTypeManger(this)
	}
}

private const val str = "1234567890qwertyuiopasdfghjklzxcvbnm"
fun randomString(size: Int): String {
	val sb = StringBuilder()
	repeat(size) {
		sb.append(str[Random.nextInt(str.length)])
	}
	return sb.toString()
}
