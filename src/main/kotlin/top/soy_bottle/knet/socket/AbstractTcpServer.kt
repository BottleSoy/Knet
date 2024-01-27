package top.soy_bottle.knet.socket

import kotlinx.coroutines.*
import java.net.ServerSocket

abstract class AbstractTcpServer<Connection : AbstractTcpServerConnection> : CoroutineScope {
	abstract val config: TcpServerConfig<out Connection>
	protected val server = BufferedServerSocket()
	val clients = arrayListOf<Connection>()
	
	fun bind(): Deferred<Boolean> = this.async {
		bindSync()
	}
	
	open suspend fun bindSync(): Boolean {
		return withContext(Dispatchers.IO) {
			try {
				server.bind(config.address)
				true
			} catch (e: Exception) {
				false
			}
		}
	}
	
	protected lateinit var socketAcceptor: Job
	
	/**
	 * 启动tcp服务
	 */
	abstract fun start()
	
	val closed = server.isClosed
	fun javaServer() = server
	
	fun close() {
		server.close()
		this.cancel("server close")
	}
}