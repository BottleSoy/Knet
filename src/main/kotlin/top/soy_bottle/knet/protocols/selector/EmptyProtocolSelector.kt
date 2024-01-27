package top.soy_bottle.knet.protocols.selector

import top.soy_bottle.knet.protocols.Connection
import java.lang.RuntimeException

object EmptyProtocolSelector : ProtocolSelector {
	override fun selectProtocol(connection: Connection) = Result.failure<String>(RuntimeException())
	override fun toString() = "EmptyProtocolSelector"
}