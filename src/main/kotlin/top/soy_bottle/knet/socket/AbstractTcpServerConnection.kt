package top.soy_bottle.knet.socket

abstract class AbstractTcpServerConnection(
	val server: AbstractTcpServer<out AbstractTcpServerConnection>,
	val socket: BufferedSocket,
) {
	val attributes: HashMap<String, Any> = hashMapOf()
	open val output = socket.getOutputStream()
	open val input = socket.getInputStream()
	
	fun close() {
		socket.close()
	}
}