package top.soy_bottle.knet.protocols.selector

import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.protocols.ProtocolSystem
import java.lang.RuntimeException

/**
 * 基于协议检测的协议选择器
 */
class DetectionProtocolSelector(
	val protocols: List<String>,
	val fallbackProtocol: String?,
) : ProtocolSelector {
	
	override fun selectProtocol(connection: Connection): Result<String> {
		protocols.forEach { name ->
			ProtocolSystem.getProtocol<AbstractProtocol<*>>(name)?.let { protocol ->
				if (protocol.detect(connection)) {
					return Result.success(protocol.name)
				}
			}
		}
		if (fallbackProtocol != null) {
			return Result.success(fallbackProtocol)
		} else {
			return Result.failure(RuntimeException("No protocol matches connection:$connection"))
		}
	}
	
	companion object {
	
	}
}