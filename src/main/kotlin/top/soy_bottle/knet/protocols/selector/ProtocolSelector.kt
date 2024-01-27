package top.soy_bottle.knet.protocols.selector

import top.soy_bottle.knet.config.JSFunction
import top.soy_bottle.knet.config.ListConfig
import top.soy_bottle.knet.config.stringList
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.protocols.ProtocolSystem
import kotlin.coroutines.Continuation

interface ProtocolSelector {
	fun selectProtocol(connection: Connection): Result<String>
	
	/**
	 * 选择协议并且直接处理
	 */
	fun selectAndHandle(connection: Connection): Result<String> = runCatching {
		val res = selectProtocol(connection).getOrThrow()
		println("$connection select to $res...")
		ProtocolSystem.protocols[res]!!.directHandle(connection)
		res
	}
	
	companion object {
		fun of(obj: Any?): ProtocolSelector? {
			return when (obj) {
				null -> EmptyProtocolSelector
				is String -> DirectProtocolSelector(obj)
				is JSFunction -> JSProtocolSelector(obj)
				is ListConfig -> DetectionProtocolSelector(obj.stringList(), null)
				
				else -> null
			}
		}
	}
}