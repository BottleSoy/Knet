package top.soy_bottle.knet.protocols.knet

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.utils.markWith
import top.soy_bottle.knet.utils.readString

class KNetProtocol(override val name: String) : AbstractProtocol<KNetConnection>,
	CoroutineScope by CoroutineScope(Dispatchers.IO) {
	override val type: String = "KNET"
	override val connections = hashSetOf<KNetConnection>()
	
	override fun detect(connection: Connection): Boolean {
		connection.input.markWith(4) {
			if (available() < 4) return false
			return readString(4) == "KNET"
		}
	}
	
	@Throws(Exception::class)
	override fun directHandle(connection: Connection) {
		val c = KNetConnection(this, connection)
		connections += c
	}
}