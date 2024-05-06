package top.soy_bottle.knet.protocols.kcp

import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection

class KcpProtocol(override val name: String) : AbstractProtocol<KcpConnection> {
	override val type: String = "kcp"
	override val connections: HashSet<KcpConnection> = hashSetOf()
	
	override fun directHandle(connection: Connection) {
		connections += KcpConnection(connection, this)
	}
}