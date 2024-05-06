package top.soy_bottle.knet.mappers

import top.soy_bottle.knet.protocols.knet.KNetProtocol
import top.soy_bottle.knet.protocols.knet.ServerKNetConnection
import top.soy_bottle.knet.utils.Env
import top.soy_bottle.knet.utils.Scope
import top.soy_bottle.knet.utils.coroutine.AsyncJob

@Env(Scope.Server)
abstract class ServerMapper(
	val protocol: KNetProtocol,
	val client: ServerKNetConnection,
	val id: Int,
) {
	
	abstract fun bind(): AsyncJob<Result<Unit>>
	
	abstract fun close()
	abstract override fun toString(): String
}