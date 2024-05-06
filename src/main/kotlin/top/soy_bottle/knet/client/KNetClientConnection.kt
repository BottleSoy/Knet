package top.soy_bottle.knet.client

import top.soy_bottle.knet.address.toAddress
import top.soy_bottle.knet.logger
import top.soy_bottle.knet.mappers.packet.Packet
import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.SizeLimitedOutputStream
import top.soy_bottle.knet.utils.coroutine.BasicJob
import top.soy_bottle.knet.utils.limitedBuffer
import top.soy_bottle.knet.utils.tls.defaultSSLContext
import top.soy_bottle.knet.utils.writeString
import java.io.IOException
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import javax.net.ssl.SSLSocket

class KNetClientConnection(val client: KNetClient, val config: ClientConnectionConfig) {
	val packetQueue = LinkedBlockingQueue<Packet>() // 创建一个无限容量的 Channel
	
	lateinit var input: SizeLimitedInputStream
		private set
	lateinit var output: SizeLimitedOutputStream
		private set
	
	lateinit var socket: Socket
	var writingJob: BasicJob? = null
	fun start() {
		BasicJob {
			try {
				val s = Socket()
				s.connect(config.address.toAddress())
				socket = s
				config.tls?.let { tls ->
					val host = tls.serverName ?: s.inetAddress.hostName
					val tlsSocket = defaultSSLContext.socketFactory.createSocket(s, host, s.port, false) as SSLSocket
					tlsSocket.sslParameters = tlsSocket.sslParameters.apply {
						this.protocols = tls.versions
					}
					tlsSocket.startHandshake()
					socket = tlsSocket
				}
				
				input = socket.getInputStream().limitedBuffer()
				output = socket.getOutputStream().limitedBuffer()
				
				output.writeString("KNET", 4)
				output.writeString(config.token)
				output.writeString(config.name)
				
				output.flush()
				logger.info("{C->S} auth data send")
				startWriting()
				client.packetManager.sendPacket(client.mappers.createMappersPacket())
				while (true) {
					logger.info("{C->S} reading...")
					client.packetManager.readPacketAndHandle(input)
				}
			} catch (e: IOException) {
				close(e)
			}
		}
	}
	
	@Synchronized
	fun close(e: IOException) {
		logger.warn("连接出错:${e.message}，5秒后重连")
		packetQueue.clear()
		writingJob?.jobThread?.interrupt()
		Thread.sleep(5000)
		start()
	}
	
	private fun startWriting() {
		writingJob = BasicJob { //发送包任务
			try {
				while (true) {
					val packet = packetQueue.take()
					client.packetManager.writePacket(output, packet)
				}
			} catch (_: InterruptedException) {
			}
		}
	}
}

data class ClientConnectionConfig(
	val name: String,
	val address: String,
	val token: String,
	val tls: Tls?,
) {
	class Tls(
		val serverName: String? = null,
		val versions: Array<String> = defaultSSLContext.defaultSSLParameters.protocols,
	)
}
