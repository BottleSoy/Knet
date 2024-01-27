package top.soy_bottle.knet.mappers.tcp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import top.soy_bottle.knet.logger
import top.soy_bottle.knet.socket.TcpClient
import top.soy_bottle.knet.utils.Copier
import top.soy_bottle.knet.utils.readRaw
import top.soy_bottle.knet.utils.writeByteArray


class ClientTcpConnection(val mapper: ClientTcpConnectionMapper, val id: Int, val onComplete: (ClientTcpConnection) -> Unit = {}) :
	CoroutineScope by CoroutineScope(Dispatchers.IO) {
	
	var tcpClient: TcpClient = TcpClient(mapper.address, this)
	
	init {
		launch {
			val res = tcpClient.start().await()
			if (res.isSuccess) {
				onComplete(this@ClientTcpConnection)
				val r = runCatching {
					val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
					var bytes = tcpClient.`in`.read(buffer)
					while (bytes >= 0) {
						mapper.sendPacket(ClientTcpDataPacket(id, buffer, bytes))
						bytes = tcpClient.`in`.read(buffer)
					}
				}
				mapper.sendPacket(ClientTcpClosePacket(id, r.isSuccess))
			} else {
				mapper.sendPacket(ClientTcpClosePacket(id, false))
			}
			close()
		}
	}
	
	fun close() {
		mapper.connections.remove(id)
		tcpClient.close()
		this.cancel("closed")
	}
	
	fun sendData(data: ByteArray) {
		runCatching {
			tcpClient.`out`.writeByteArray(data)
			tcpClient.`out`.flush()
		}.onFailure {
			mapper.sendPacket(ClientTcpClosePacket(id, false))
			close()
		}
	}
}