package top.soy_bottle.knet.protocols.tls

import top.soy_bottle.knet.protocols.BasicConnection
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.protocols.tls.packet.TLSClientHello
import top.soy_bottle.knet.utils.limitedBuffer
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.nio.channels.Channels
import java.util.Arrays
import javax.net.ssl.HandshakeCompletedEvent
import javax.net.ssl.SSLEngine
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
	val factory: SSLSocketFactory,
	connectionConfig: (SSLParameters) -> SSLParameters = { it },
	onComplete: (HandshakeCompletedEvent, TLSConnection) -> Unit = { _, _ -> },
) : BasicConnection(connection) {
	/**
	 * 表示是否已经完成了TLS连接
	 */
	var completed = false
		private set
	
	val sslSocket: SSLSocket
	
	private val vSocket = object : Socket() {
		override fun getInputStream() = connection.input
		override fun getOutputStream() = connection.output
		override fun isConnected() = !isClosed
		
		override fun isClosed() = this@TLSConnection.isClosed
	}
	
	init {
		
		sslSocket = factory.createSocket(vSocket, null, 0, false) as SSLSocket
		sslSocket.useClientMode = false
		
		sslSocket.sslParameters = connectionConfig(sslSocket.sslParameters)
		
		sslSocket.addHandshakeCompletedListener {
			completed = true
			onComplete(it, this)
		}
		sslSocket.startHandshake()
	}
	
	
	override val input = sslSocket.inputStream.limitedBuffer()
	override val output = sslSocket.outputStream.limitedBuffer()
	
	override fun close() {
		super.close()
		sslSocket.close()
	}
	
}