package top.soy_bottle.knet.protocols.tls

import top.soy_bottle.knet.protocols.BasicConnection
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.protocols.tls.packet.TLSClientHello
import top.soy_bottle.knet.utils.limitedBuffer
import javax.net.ssl.HandshakeCompletedEvent
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class TLSConnection(
	override val protocol: TLSProtocol,
	connection: Connection,
	val helloPacket: TLSClientHello,
	/**
	 * 创建SSLSocket的工厂
	 */
	factory: SSLSocketFactory,
	connectionConfig: (SSLParameters) -> SSLParameters = { it },
	onComplete: (HandshakeCompletedEvent, TLSConnection) -> Unit = { _, _ -> },
) : BasicConnection(connection) {
	/**
	 * 表示是否已经完成了TLS连接
	 */
	var completed = false
		private set
	
	private val sslSocket: SSLSocket
	
	init {
		val pSocket = connection.javaSocket()
		
		sslSocket = factory.createSocket(pSocket, pSocket.inetAddress.hostAddress, pSocket.port, false) as SSLSocket
		sslSocket.useClientMode = false
		
		sslSocket.sslParameters = connectionConfig(sslSocket.sslParameters)
		
		sslSocket.addHandshakeCompletedListener {
			completed = true
			onComplete(it, this)
		}
		sslSocket.startHandshake()
	}
	
	override fun javaSocket() = sslSocket
	
	override val input = sslSocket.inputStream.limitedBuffer()
	override val output = sslSocket.outputStream.limitedBuffer()
	
	override fun close() {
		super.close()
		sslSocket.close()
	}
	
}