package top.soy_bottle.knet.socket

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TcpServer(override val config: TcpServerConfig<TcpServerConnection>, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) :
	AbstractTcpServer<TcpServerConnection>(), CoroutineScope by scope {
	override fun start() {
		socketAcceptor = this.launch(Dispatchers.IO) {
			while (clients.size < config.maxConnection) {
				val socket = server.accept()
				clients += TcpServerConnection(this@TcpServer, socket).apply {
					config.handler.onNewClient(this)
				}
			}
		}
	}
}