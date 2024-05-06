package top.soy_bottle.knet.mappers.tcp

import top.soy_bottle.knet.address.toAddress
import top.soy_bottle.knet.logger
import top.soy_bottle.knet.protocols.knet.ServerKNetConnection
import top.soy_bottle.knet.mappers.ServerMapper
import top.soy_bottle.knet.protocols.ConnectionInfo
import top.soy_bottle.knet.protocols.ProtocolSystem
import top.soy_bottle.knet.protocols.knet.KNetProtocol
import top.soy_bottle.knet.socket.ConnectionManager
import top.soy_bottle.knet.socket.TcpServer
import top.soy_bottle.knet.socket.TcpServerConfig
import top.soy_bottle.knet.socket.TcpServerConnection
import top.soy_bottle.knet.utils.*
import top.soy_bottle.knet.utils.coroutine.AsyncJob
import top.soy_bottle.knet.utils.coroutine.BasicJob
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue


@Env(Scope.Server)
class ServerTcpMapper(
	protocol: KNetProtocol,
	client: ServerKNetConnection,
	id: Int,
	val name: String,
	val forward: String,
) : ServerMapper(protocol, client, id) {
	val tcpManager = client.mapperManager["tcp"] as ServerTcpTypeManager
	var server: TcpServer? = null
	val mappedProtocol = MappedTcpProtocol(this)
	override fun bind(): AsyncJob<Result<Unit>> {
		//注册协议
		ProtocolSystem += mappedProtocol
		
		//注册监听服务器
		if (forward.isNotEmpty()) {
			val server = TcpServer(object : TcpServerConfig() {
				override val address = forward.toAddress()
				
				override val handler = ConnectionManager {
					synchronized(tcpManager.connection) {
						val id = tcpManager.idCount++
						
						client.sendPacket(
							ServerNewTcpConnectionPacket(
								this@ServerTcpMapper.id, id, ConnectionInfo(
									it.socket.remoteAddress.toAddress(true),
									it.socket.remoteAddress.toAddress(true),
									it.socket.localAddress.toAddress(true),
									listOf()
								)
							)
						)
						tcpManager.connections[id] = object : ServerTcpConnection {
							val remote = it
							var closed = false
							val actionQueue = LinkedBlockingQueue<TcpAction>()
							override val connectionId: Int = id
							override fun start() {
								val readerJob = BasicJob {
									val res = runCatching {
										val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
										var bytes = remote.input.read(buffer)
										while (bytes >= 0) {
											client.sendPacket(ServerTcpDataPacket(id, buffer.copyOf(), bytes))
											bytes = remote.input.read(buffer)
										}
									}
									close(res.isSuccess)
								}
								val actionJob = BasicJob {
									try {
										while (true) {
											when (val action = actionQueue.take()) {
												is SendData -> {
													val (data, len) = action
													remote.output.writeByteArray(data, len)
													remote.output.flush()
												}
												
												ReqeustClose -> {
													close(true)
													break
												}
											}
										}
									} catch (e: IOException) {
										close()
									}
								}
							}
							
							@Synchronized
							override fun close(normalClose: Boolean) {
								if (closed) return
								closed = true
								if(!requestClose)
									removeConnection(this, normalClose)
								remote.close()
							}
							
							var requestClose = false
							override fun requestClose() {
								requestClose = true
								actionQueue.put(ReqeustClose)
							}
							
							override fun send(data: ByteArray, len: Int) {
								try {
									remote.output.writeByteArray(data, len).flush()
									logger.info("{S->U:${it.socket.remoteAddress}} write data to user with:$len")
								} catch (e: IOException) {
									close(false)
								}
							}
						}
					}
				}
			})
			this.server = server
			
			return AsyncJob {
				val res = server.bind().await()
				if (res.isSuccess) {
					server.start()
				}
				res
			}
		}
		
		
		return AsyncJob { Result.success(Unit) }
	}
	
	override fun close() {
		ProtocolSystem -= mappedProtocol
		server?.close()
	}
	
	fun removeConnection(connection: ServerTcpConnection, normalClose: Boolean) {
		client.sendPacket(ServerTcpClosePacket(connection.connectionId, normalClose))
		tcpManager.connections.remove(connection.connectionId)
		
	}
	
	override fun toString(): String = "ServerTcpMapper(id=$id, name=$name)"
	
}

