package top.soy_bottle.knet.mappers.tcp

import top.soy_bottle.knet.logger
import top.soy_bottle.knet.socket.TcpClient
import top.soy_bottle.knet.utils.coroutine.BasicJob
import top.soy_bottle.knet.utils.writeByteArray
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue


class ClientTcpConnection(
	val mapper: ClientTcpMapper, val id: Int,
	val onComplete: (ClientTcpConnection) -> Unit = {},
) {
	
	var tcpClient: TcpClient = TcpClient(mapper.address)
	val typeManager = mapper.client.types["tcp"] as ClientTcpTypeManager
	var closed = false
		private set
	var actionQueue = LinkedBlockingQueue<TcpAction>()
	
	
	val readerJob: BasicJob = BasicJob {
		logger.info("will start client for ${mapper.address}")
		val res = tcpClient.start()
		res.onSuccess {
			onComplete(this@ClientTcpConnection)
			val r = runCatching {
				val buffer = ByteArray(4096)
				while (!closed) {
					val bytes = tcpClient.input.read(buffer)
					logger.info("{C->P} read bytes with:$bytes")
					if (bytes > 0 && !closed) {
						mapper.sendPacket(ClientTcpDataPacket(id, buffer.copyOf(), bytes))
					} else {
						break
					}
				}
			}
		}
		close()
	}
	val actionJob: BasicJob = BasicJob {
		try {
			while (true) {
				when (val action = actionQueue.take()) {
					is SendData -> {
						val (data, len) = action
						tcpClient.output.writeByteArray(data, len)
						tcpClient.output.flush()
					}
					
					ReqeustClose -> {
						close()
						break
					}
				}
			}
		} catch (e: IOException) {
			close()
		}
	}
	
	
	@Synchronized
	fun close() {
		if (closed) return
		closed = true
		typeManager.connections.remove(id)
		mapper.sendPacket(ClientTcpClosePacket(id, false))
		tcpClient.close()
	}
	
	fun requestClose() {
		actionQueue.put(ReqeustClose)
	}
	
	fun sendData(data: ByteArray, len: Int = data.size) {
		if (closed) return
		actionQueue.put(SendData(data, len))
	}
}