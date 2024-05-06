package top.soy_bottle.knet.protocols.udp

import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection

class UdpProtocol(override val name: String) : AbstractProtocol<UdpConnection> {
	override val type: String = "udp"
	
	override val connections: HashSet<UdpConnection> = hashSetOf()
	
	override fun directHandle(connection: Connection)=
		throw IllegalAccessException("Udp Protocol can not handle Connection")
	
}