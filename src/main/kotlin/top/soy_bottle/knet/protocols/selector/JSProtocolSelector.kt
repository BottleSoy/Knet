package top.soy_bottle.knet.protocols.selector

import top.soy_bottle.knet.config.JSFunction
import top.soy_bottle.knet.protocols.Connection

class JSProtocolSelector(val jsFunction: JSFunction) : ProtocolSelector {
	override fun selectProtocol(connection: Connection) = runCatching {
		jsFunction.invoke(connection) as String
	}
	
}
