package top.soy_bottle.knet.mappers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import top.soy_bottle.knet.utils.Env
import top.soy_bottle.knet.utils.Scope

@Env(Scope.Server)
abstract class ServerMapper(
	val client: MapperClientConnection,
	val id: Int,
	val msg: ByteArray,
) {
	
	abstract fun bind(): Deferred<Boolean>
}