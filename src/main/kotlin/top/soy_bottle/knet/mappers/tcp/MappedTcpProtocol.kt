package top.soy_bottle.knet.mappers.tcp

import top.soy_bottle.knet.address.toAddress
import top.soy_bottle.knet.logger
import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.BasicConnection
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.protocols.ConnectionInfo
import top.soy_bottle.knet.protocols.haproxy.realAddress
import top.soy_bottle.knet.utils.coroutine.BasicJob
import top.soy_bottle.knet.utils.writeByteArray
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue

/**
 * 被映射的TCP协议所构建的连接
 */
class MappedTcpConnection(
	connection: Connection,
	override val protocol: MappedTcpProtocol,
	override val connectionId: Int,
) : BasicConnection(connection), ServerTcpConnection {
	var closed = false
	val actionQueue = LinkedBlockingQueue<TcpAction>()
	
	
	init {
		protocol.mapper.client.sendPacket(
			ServerNewTcpConnectionPacket(
				protocol.mapper.id,
				connectionId,
				ConnectionInfo(
					connection.realAddress!!.toAddress(true),
					connection.remoteAddress!!.toAddress(true),
					connection.localAddress!!.toAddress(true),
					connection.parents.map { it.protocol.name }
				)
			)
		)
	}
	
	override fun start() {
		val readerJob = BasicJob {
			try {
				val bytes = ByteArray(1024)
				var count = input.read(bytes)
				
				while (count > 0) {
					logger.info("{U->S:$connection} write data with:$count")
					protocol.mapper.client.sendPacket(ServerTcpDataPacket(connectionId, bytes.copyOf(), count))
					count = input.read(bytes)
				}
			} catch (e: IOException) {
				close()
			}
		}
		val actionJob = BasicJob {
			try {
				while (true) {
					when (val action = actionQueue.take()) {
						is SendData -> {
							val (data, len) = action
							output.writeByteArray(data, len)
							output.flush()
						}
						
						ReqeustClose -> {
							close(true)
							break
						}
					}
				}
			} catch (e: IOException) {
				close()
			}
		}
	}
	
	@Synchronized
	override fun close(normalClose: Boolean) {
		if (closed) return
		if (!requestClose) {
			closed = true
			protocol.mapper.removeConnection(this, normalClose)
		}
		super.close()
	}
	
	var requestClose = false
	override fun requestClose() {
		requestClose = true
		actionQueue.put(ReqeustClose)
	}
	
	override fun send(data: ByteArray, len: Int) {
		actionQueue.put(SendData(data, len))
	}
}

class MappedTcpProtocol(val mapper: ServerTcpMapper) : AbstractProtocol<MappedTcpConnection> {
	override val type = "mapped-Tcp"
	override val name: String = "${mapper.protocol.name}.${mapper.name}"
	override val connections: HashSet<MappedTcpConnection> = HashSet()
	
	override fun directHandle(connection: Connection) {
		synchronized(mapper.tcpManager.connections) {
			val id = ++mapper.tcpManager.idCount
			val conn = MappedTcpConnection(connection, this@MappedTcpProtocol, id)
			connections += conn
			mapper.tcpManager.connections[id] = conn
			conn
		}
		
	}
}