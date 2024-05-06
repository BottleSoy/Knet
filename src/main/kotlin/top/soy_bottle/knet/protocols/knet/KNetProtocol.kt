package top.soy_bottle.knet.protocols.knet

import top.soy_bottle.knet.mappers.MapperType
import top.soy_bottle.knet.mappers.ServerMapperManager
import top.soy_bottle.knet.mappers.tcp.TcpMapperType
import top.soy_bottle.knet.mappers.udp.UdpMapperType
import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.utils.markWith
import top.soy_bottle.knet.utils.readString

class KNetProtocol(override val name: String) : AbstractProtocol<KNetConnection> {
	override val type: String = "KNET"
	val token = "test-token" //FIXME use config var
	override val connections = hashSetOf<KNetConnection>()
	val connectionsByName = hashMapOf<String, KNetConnection>()
	val types = hashMapOf<String, MapperType>()
	
	init {
		types["tcp"] = TcpMapperType
		types["udp"] = UdpMapperType
	}
	
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