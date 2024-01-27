package top.soy_bottle.knet.socket

import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.SizeLimitedOutputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.*
import javax.net.ServerSocketFactory

/**
 * 提供缓冲区的ServerSocket
 */
class BufferedServerSocket : ServerSocket {
	constructor()
	constructor(port: Int) : super(port)
	constructor(port: Int, backlog: Int) : super(port, backlog)
	constructor(port: Int, backlog: Int, ifAddress: InetAddress?) : super(port, backlog, ifAddress)
	
	override fun accept(): BufferedSocket {
		if (isClosed) throw SocketException("Socket is closed")
		if (!isBound) throw SocketException("Socket is not bound yet")
		val s = BufferedSocket()
		implAccept(s)
		return s
	}
}

class BufferedSocket : Socket() {
	private val bin by lazy { SizeLimitedInputStream(super<Socket>.getInputStream()) }
	
	private val bout by lazy { SizeLimitedOutputStream(super<Socket>.getOutputStream()) }
	
	override fun getInputStream(): SizeLimitedInputStream = bin
	
	override fun getOutputStream(): SizeLimitedOutputStream = bout
	
}

object BufferedServerSocketFactory : ServerSocketFactory() {
	
	override fun createServerSocket(port: Int) = BufferedServerSocket(port)
	
	override fun createServerSocket(port: Int, backlog: Int) = BufferedServerSocket(port, backlog)
	
	override fun createServerSocket(port: Int, backlog: Int, ifAddress: InetAddress?) =
		BufferedServerSocket(port, backlog, ifAddress)
	
}