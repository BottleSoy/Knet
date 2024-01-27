package top.soy_bottle.knet.protocols

import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.SizeLimitedOutputStream
import java.net.Socket

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
	
	abstract fun javaSocket(): Socket
	
	abstract val input: SizeLimitedInputStream
	
	abstract val output: SizeLimitedOutputStream
	
	open fun close() {
		protocol.connections.remove(this)
	}
	
	final override fun hashCode(): Int {
		return javaSocket().remoteSocketAddress.hashCode() * (parents.size + 1)
	}
	
	abstract override fun toString(): String
	final override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is Connection) return false
		
		if (javaSocket() != other.javaSocket()) return false
		if (parents.size != other.parents.size) return false
		
		return true
	}
}