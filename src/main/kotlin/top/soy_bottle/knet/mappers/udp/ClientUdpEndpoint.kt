package top.soy_bottle.knet.mappers.udp

import java.net.DatagramSocket

data class ClientUdpEndpoint(
	val connectionId: Int,
	val mapper: ClientUdpMapper,
	val socket: DatagramSocket,
	val readSize: Int = DEFAULT_BUFFER_SIZE,
) {
	var lastRead: Long = System.currentTimeMillis()
	var lastWrite: Long = System.currentTimeMillis()
//
//	var invalidJob = BasicJob {
//		delay(30000)
//
//	}
//
//	fun writeData(bytes: ByteArray) {
//		if (lastWrite < lastRead) {
//			invalidJob.cancel()
//			invalidJob = BasicJob {
//				delay(30000)
//
//			}
//		}
//		lastWrite = System.currentTimeMillis()
//	}
//
//	val readerJob = BasicJob {
//		val packet = DatagramPacket(ByteArray(readSize), readSize)
//		while (true) {
//			socket.receive(packet)
//
//			if (lastRead < lastWrite) {
//				invalidJob.cancel()
//				invalidJob = scope.launch {
//					delay(30000)
//
//				}
//			}
//			lastRead = System.currentTimeMillis()
//
//		}
//	}
	
}