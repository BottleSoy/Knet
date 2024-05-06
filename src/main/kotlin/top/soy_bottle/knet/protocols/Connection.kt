package top.soy_bottle.knet.protocols

import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.SizeLimitedOutputStream
import java.net.Socket
import java.net.SocketAddress
import java.nio.channels.SocketChannel
import kotlin.time.times

/**
 * 一个连接
 */
abstract class Connection {
	/**
	 * 连接所在的协议
	 */
	abstract val protocol: AbstractProtocol<*>
	
	/**
	 * 其父辈的连接
	 */
	abstract val parents: List<Connection>
	
	
	abstract val input: SizeLimitedInputStream
	
	abstract val output: SizeLimitedOutputStream
	
	var isClosed = false
		private set
	
	open fun close() {
		isClosed = true
		protocol.connections.remove(this)
	}
	
	abstract val remoteAddress: SocketAddress?
	
	abstract val localAddress: SocketAddress?
	
	val createTime = System.currentTimeMillis()
	
	final override fun hashCode(): Int {
		return (remoteAddress.hashCode() + 1) *
			(localAddress.hashCode() + 1) *
			(parents.size + 1) *
			createTime.toInt()
	}
	
	abstract override fun toString(): String
	final override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Connection) return false
		
		if (remoteAddress != other.remoteAddress) return false
		if (localAddress != other.localAddress) return false
		if (createTime != other.createTime) return false
		
		if (parents.size != other.parents.size) return false
		
		return true
	}
}