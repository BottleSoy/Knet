package top.soy_bottle.knet.protocols.selector

import top.soy_bottle.knet.protocols.Connection

class DirectProtocolSelector(private val nextProtocol: String) : ProtocolSelector {
	override fun selectProtocol(connection: Connection) = Result.success(nextProtocol)
	override fun toString() = "DirectProtocolSelector->$nextProtocol"
}