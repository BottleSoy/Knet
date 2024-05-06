package top.soy_bottle.knet.socket

import top.soy_bottle.knet.logger
import top.soy_bottle.knet.utils.coroutine.AsyncJob
import top.soy_bottle.knet.utils.coroutine.BasicJob
import java.nio.channels.ServerSocketChannel

class TcpServer(
	val config: TcpServerConfig,
) {
	val clients = arrayListOf<TcpServerConnection>()
	val server = ServerSocketChannel.open()
	
	fun bind(): AsyncJob<Result<Unit>> = AsyncJob {
		bindSync()
	}
	
	fun bindSync(): Result<Unit> {
		return runCatching {
			server.configureBlocking(true)
			server.bind(config.address)
		}
	}
	
	protected lateinit var socketAcceptor: BasicJob
	
	val closed get() = !server.isOpen
	fun javaServer() = server
	
	/**
	 * 启动tcp服务
	 */
	fun start() {
		socketAcceptor = BasicJob {
			while (clients.size < config.maxConnection && server.isOpen) {
				try {
					val socket = server.accept()
					logger.info("[Tcp] [Server] new Tcp connection from `${socket.remoteAddress}`")
					val connection = TcpServerConnection(this@TcpServer, socket)
					clients += connection
					BasicJob {
						config.handler.onNewClient(connection)
					}
				} catch (e: Exception) {
					e.printStackTrace()
					logger.info("Tcp Server Accept Error" + e.message)
				}
			}
		}
	}
	
	fun close() {
		server.close()
		clients.forEach {
			it.close()
		}
		logger.info("[TcpServer] Unbind ${config.address}")
	}
	
}


