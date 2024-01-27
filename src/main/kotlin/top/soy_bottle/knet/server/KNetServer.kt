package top.soy_bottle.knet.server

import top.soy_bottle.knet.KApplication
import top.soy_bottle.knet.config.JSConfig
import top.soy_bottle.knet.config.KNetConfig
import top.soy_bottle.knet.config.KTSConfig
import top.soy_bottle.knet.config.sectionList
import top.soy_bottle.knet.logger
import top.soy_bottle.knet.protocols.ProtocolFactory
import top.soy_bottle.knet.protocols.ProtocolSystem

class KNetServer(val config: KNetConfig) : KApplication {
	
	override fun start() {
		logger.info("[Server] 服务端启动中...")
		when(config){
			is JSConfig->{
				config.getList("sections").sectionList().forEach {
					ProtocolSystem += ProtocolFactory.createProtocol(it)
				}
			}
			
			is KTSConfig -> config.execute()
		}
		logger.info("[Server] 服务端启动完毕！")
		while (true)
			Thread.sleep(100000)
	}
}