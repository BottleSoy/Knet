package top.soy_bottle.knet.mappers.tcp

import kotlinx.coroutines.*
import top.soy_bottle.knet.address.toAddress
import top.soy_bottle.knet.logger
import top.soy_bottle.knet.mappers.MapperClientConnection
import top.soy_bottle.knet.server.KNetServer
import top.soy_bottle.knet.mappers.ServerMapper
import top.soy_bottle.knet.protocols.ConnectionInfo
import top.soy_bottle.knet.socket.ConnectionManager
import top.soy_bottle.knet.socket.TcpServer
import top.soy_bottle.knet.socket.TcpServerConfig
import top.soy_bottle.knet.socket.TcpServerConnection
import top.soy_bottle.knet.utils.*


@Env(Scope.Server)
class ServerTcpConnectionMapper(client: MapperClientConnection, id: Int, msg: ByteArray) :
	ServerMapper(client, id, msg), CoroutineScope by CoroutineScope(Dispatchers.IO) {
	val name: String
	val forward: String
	
	init {
		val i = msg.inputStream()
		name = i.readString()
		forward = i.readString()
	}
	
	val clients = hashMapOf<Int, ServerTcpConnection>()
	var idCount = 0
	var server: TcpServer? = null
	
	override fun bind(): Deferred<Boolean> {
		if (forward.isNotEmpty()) {
			val server = TcpServer(object : TcpServerConfig<TcpServerConnection>() {
				override val address = forward.toAddress()
				
				override val handler = ConnectionManager<TcpServerConnection> {
					synchronized(clients) {
						val id = idCount++
						
						client.sendPacket(
							ServerNewTcpConnectionPacket(
								this@ServerTcpConnectionMapper.id, id, ConnectionInfo(
									it.socket.remoteSocketAddress.toAddress(true),
									it.socket.remoteSocketAddress.toAddress(true),
									it.socket.localSocketAddress.toAddress(true),
									listOf()
								)
							)
						)
						clients[id] = object : ServerTcpConnection {
							val con = it
							
							init {
								launch {
									val res = runCatching {
										val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
										var bytes = con.input.read(buffer)
										while (bytes >= 0) {
											client.sendPacket(ServerTcpDataPacket(id, buffer, bytes))
											bytes = con.input.read(buffer)
										}
									}
									client.sendPacket(ServerTcpClosePacket(id, res.isSuccess))
								}
							}
							
							override fun close() {
								con.close()
							}
							
							override fun send(data: ByteArray) {
								con.output.writeByteArray(data)
									.flush()
							}
						}
						
					}
				}
				
			}, this)
			this.server = server
			return server.bind()
		}
		return async { true }
	}
	
	fun onPacket(packet: TcpClientPacket) {
		when (packet) {
			is ClientTcpClosePacket -> clients.remove(packet.connectionId)?.close()
			
			is ClientTcpDataPacket -> clients[packet.connectionId]?.send(packet.data)
		}
	}
	
}

