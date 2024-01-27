package top.soy_bottle.knet.socket

/**
 * 客户端的管理器
 */
fun interface ConnectionManager<Connection : AbstractTcpServerConnection> {
	fun onNewClient(connection: Connection)
}