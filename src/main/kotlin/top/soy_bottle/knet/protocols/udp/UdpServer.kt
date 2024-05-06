package top.soy_bottle.knet.protocols.udp

import java.net.DatagramSocket
import java.net.SocketAddress

class UdpServer(val bindAddr: SocketAddress) {
	val server = DatagramSocket()
	fun start() {
		server.bind(bindAddr)
		
	}
}