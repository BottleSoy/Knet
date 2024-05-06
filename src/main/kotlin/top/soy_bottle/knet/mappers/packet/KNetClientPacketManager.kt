package top.soy_bottle.knet.mappers.packet

import top.soy_bottle.knet.client.KNetClient
import top.soy_bottle.knet.logger

class KNetClientPacketManager(val client: KNetClient) : PacketManager() {
	var ping: Int = -1
	
	init {
		registerSendPacket<ClientPing>()
		registerSendPacket<ClientMappers>()
		registerSendPacket<ZipPacket>()
		
		registerRecivePacket(::ServerPong) {
			val time = System.currentTimeMillis()
			ping = (time - it.serverTime).toInt()
		}
		registerRecivePacket(::ServerMapResult) {
			logger.info("映射:${it.id},成功:${it.code == 0},消息:${it.errorMsg}")
		}
		registerRecivePacket(::ZipPacket) {
			this.readPacket(it.data.inputStream())
		}
	}
	
	fun start() {
		/**
		 * 心跳包任务
		 */
//		client.launch {
//			var id = 0
//			while (true) {
//				delay(30000)
//				sendPacket(ClientPing(id++, System.currentTimeMillis()))
//			}
//		}
	
	}
	
	fun sendPacket(packet: Packet) {
		client.connection.packetQueue.put(packet)
	}
}
