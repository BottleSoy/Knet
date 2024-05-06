package top.soy_bottle.knet.protocols.udp

import java.net.DatagramSocket

class UdpServerConnection(
	val server: UdpServer,
	val datagram: DatagramSocket,
) {

}