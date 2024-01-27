package top.soy_bottle.knet.protocols.minecraft

import top.soy_bottle.knet.protocols.BasicConnection
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.SizeLimitedOutputStream
import java.net.Socket

class MinecraftConnection(
	override val protocol: MinecraftProtocol,
	connection: Connection,
	val handshake: MinecraftHandshakePacket,
) : BasicConnection(connection) {
	
	override fun close() {
	
	}
	
}