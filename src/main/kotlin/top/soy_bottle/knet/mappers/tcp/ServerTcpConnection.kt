package top.soy_bottle.knet.mappers.tcp

interface ServerTcpConnection {
	fun close()
	fun send(data: ByteArray)
}