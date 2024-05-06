package top.soy_bottle.knet.protocols.selector

import top.soy_bottle.knet.config.*
import top.soy_bottle.knet.logger
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
		logger.info("$connection select to $res...")
		ProtocolSystem.protocols[res]!!.directHandle(connection)
		res
	}.onFailure {
		logger.warn("$connection select to ${selectProtocol(connection).getOrThrow()} has error $it")
		it.printStackTrace()
	}
	
	companion object {
		fun of(obj: Any?): ProtocolSelector? {
			return when (obj) {
				null -> EmptyProtocolSelector
				is String -> DirectProtocolSelector(obj)
				is JSFunction -> JSProtocolSelector(obj)
				is ListConfig -> DetectionProtocolSelector(obj.stringList(), null)
				is SectionConfig -> {
					val protocols = obj["protocols"]
					if (protocols is ListConfig) {
						return DetectionProtocolSelector(protocols.stringList(), obj.getStringOrNull("fallback"))
					}
					null
				}
				else -> null
			}
		}
	}
}