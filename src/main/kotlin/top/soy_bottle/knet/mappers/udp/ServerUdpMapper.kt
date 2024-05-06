package top.soy_bottle.knet.mappers.udp

import top.soy_bottle.knet.address.toAddress
import top.soy_bottle.knet.mappers.ServerMapper
import top.soy_bottle.knet.protocols.knet.KNetProtocol
import top.soy_bottle.knet.protocols.knet.ServerKNetConnection
import top.soy_bottle.knet.utils.coroutine.AsyncJob
import top.soy_bottle.knet.utils.coroutine.BasicJob
import java.net.DatagramPacket
import java.net.DatagramSocket

class ServerUdpMapper(
	protocol: KNetProtocol,
	client: ServerKNetConnection,
	id: Int,
	val name: String,
	val forward: String,
	
) : ServerMapper(protocol, client, id) {
	
	val datagramSocket = DatagramSocket()
	
	
	
	override fun bind(): AsyncJob<Result<Unit>> = AsyncJob {
		if (forward.isNotEmpty()) {
			datagramSocket.bind(forward.toAddress())
			BasicJob {
				val datagramPacket = DatagramPacket(ByteArray(4096), 4096)
				while (!client.connection.isClosed) {
					datagramSocket.receive(datagramPacket)
					client.sendPacket(
						UdpServerPacket(
							datagramPacket.socketAddress.toAddress(false),
							id,
							datagramPacket.data,
							datagramPacket.length
						)
					)
				}
			}
		}
		Result.success(Unit)
	}
	
	override fun close() {
		datagramSocket.close()
	}
	
	override fun toString()="ServerUdpMapper(id=$id, name=$name)"
}