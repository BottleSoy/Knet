package top.soy_bottle.knet.protocols.knet

import top.soy_bottle.knet.logger
import top.soy_bottle.knet.mappers.ServerMapperManager
import top.soy_bottle.knet.mappers.packet.KNetServerPacketManager
import top.soy_bottle.knet.mappers.packet.Packet
import top.soy_bottle.knet.protocols.BasicConnection
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.coroutine.BasicJob
import top.soy_bottle.knet.utils.readString
import java.util.concurrent.LinkedBlockingQueue

class KNetConnection(override val protocol: KNetProtocol, connection: Connection) :
	BasicConnection(connection), ServerKNetConnection {
	val packetManager = KNetServerPacketManager(this)
	override val mapperManager = ServerMapperManager(this)
	var name: String? = null
	val readerJob = BasicJob {
		runCatching {
			logger.info("${this@KNetConnection} start reading...")
			name = readClientAuthData(input) ?: throw IllegalAccessException("auth failed")
			synchronized(protocol.connectionsByName) {
				protocol.connectionsByName[name]?.close()
				protocol.connectionsByName[name!!] = this@KNetConnection
			}
			while (!this@KNetConnection.isClosed) {
				packetManager.readPacketAndHandle(input)
			}
		}.onFailure {
			it.printStackTrace()
			close()
		}
	}
	val packetQueue = LinkedBlockingQueue<Packet>()
	val writeJob = BasicJob {
		runCatching {
			while (!this@KNetConnection.isClosed) {
				val packet = packetQueue.take()
				packetManager.writePacket(output, packet)
			}
		}
	}
	
	private fun readClientAuthData(input: SizeLimitedInputStream): String? {
		val knet = input.readString(4)
		if (knet != "KNET") return null
		val token = input.readString()
		if (token != protocol.token) return null
		
		logger.info("$this passed auth")
		return input.readString()
	}
	
	override fun sendPacket(packet: Packet) {
		packetQueue.put(packet)
	}
	
	
	override fun close() {
		logger.info("KNet - $remoteAddress 已经离线")
		
		mapperManager.mappers.forEach { (t, mapper) ->
			logger.info("KNet - 关闭$mapper")
			mapper.close()
		}
		super.close()
		writeJob.jobThread.interrupt()
	}
	
	override fun toString(): String {
		return "KNetConnection(protocol=$protocol, name='$name')"
	}
	
}