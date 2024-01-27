package top.soy_bottle.knet.protocols.haproxy

import top.soy_bottle.knet.protocols.BasicConnection
import top.soy_bottle.knet.protocols.Connection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.UnixDomainSocketAddress

class HAProxyConnection(
	override val protocol: HAProxyMessageProtocol, connection: Connection, val proxyMessage: HAProxyMessage,
) : BasicConnection(connection)

fun Connection.lookupRealAddress(): SocketAddress {
	for (it in this.parents + this) {
		if (it is HAProxyConnection) {
			val msg = it.proxyMessage
			return when (msg.proxiedProtocol.addressFamily) {
				AddressFamily.AF_UNSPEC -> throw HAProxyProtocolException("using UNSPEC addressFamily")
				AddressFamily.AF_IPv4, AddressFamily.AF_IPv6 -> 
					InetSocketAddress(InetAddress.getByName(msg.sourceAddress), msg.sourcePort)
				AddressFamily.AF_UNIX -> UnixDomainSocketAddress.of(msg.sourceAddress)
			}
		}
	}
	return this.javaSocket().remoteSocketAddress
}
