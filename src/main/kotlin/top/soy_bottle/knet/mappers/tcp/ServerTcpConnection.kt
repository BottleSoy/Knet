package top.soy_bottle.knet.mappers.tcp

/**
 * 一个在服务端映射的连接
 */
interface ServerTcpConnection {
	val connectionId: Int
	
	/**
	 * 启动读写
	 */
	fun start()
	fun close(normalClose: Boolean = false)
	fun requestClose()
	fun send(data: ByteArray, len: Int)
}