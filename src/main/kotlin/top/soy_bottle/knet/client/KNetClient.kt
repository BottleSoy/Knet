package top.soy_bottle.knet.client

import top.soy_bottle.knet.KApplication
import top.soy_bottle.knet.config.KNetConfig

class KNetClient(val config: KNetConfig?) : KApplication {
	//	val packetManager: ClientPacketManager
//	val connection: ServerConnection
	
	val mappers: MapperService = MapperService(this)
	
	override fun start() {
	
	}
}