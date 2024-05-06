package top.soy_bottle.knet.mappers.packet

import top.soy_bottle.knet.logger
import top.soy_bottle.knet.protocols.knet.KNetConnection
import top.soy_bottle.knet.utils.coroutine.BasicJob

class KNetServerPacketManager(val connection: KNetConnection) : PacketManager() {
	var ping: Int = -1
	
	init {
		registerSendPacket<ServerPong>()
		registerSendPacket<ServerMapResult>()
		registerSendPacket<ZipPacket>()
		
		
		registerRecivePacket(::ClientPing) {
			val currentTime = System.currentTimeMillis()
			connection.sendPacket(ServerPong(it.id, currentTime))
			ping = (currentTime - it.clientTime).toInt()
		}
		registerRecivePacket(::ClientMappers) { packet ->
			val startTime = System.currentTimeMillis()
			
			packet.msgs.forEach { mapper ->
				val type = connection.mapperManager.types[mapper.type] ?: return@forEach connection.sendPacket(
					ServerMapResult(
						mapper.id,
						-1,
						0,
						"mapper type not found"
					)
				)
				val serverMapper = type.createServerMapper(connection.protocol, connection, mapper.id, mapper.data)
				connection.mapperManager.mappers[mapper.id] = serverMapper
				
				BasicJob {
					logger.info("{S:Knet:$connection} 绑定$serverMapper...")
					for (i in 0..10) {
						val (code, msg) = serverMapper.bind().await()
							.fold({ 0 to "" }, { 1 to "map action failed:${it.message}" })
						if (code == 0) {
							connection.sendPacket(
								ServerMapResult(
									mapper.id, code,
									(System.currentTimeMillis() - startTime).toInt(), msg
								)
							)
							logger.info("$serverMapper 绑定成功")
							break
						} else {
							logger.info("$serverMapper 绑定失败,5秒后重新尝试绑定...")
							Thread.sleep(5000)
						}
					}
				}
			}
		}
		registerRecivePacket(::ZipPacket) {
			this.readPacket(it.data.inputStream())
		}
	}
	
}