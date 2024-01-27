package top.soy_bottle.knet.protocols.minecraft

import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.utils.*
import java.io.IOException

class MinecraftProtocol(override val name: String) : AbstractProtocol<MinecraftConnection> {
	val protocolSelector: (Connection, MinecraftHandshakePacket) -> String = { _, _ -> "" }
	
	override val type = "Minecraft"
	
	
	override val connections = hashSetOf<MinecraftConnection>()
	
	override fun detect(connection: Connection): Boolean {
		return readMinecraftHandshake(connection.input) != null
	}
	
	override fun directHandle(connection: Connection) {
		val handshake =
			readMinecraftHandshake(connection.input) ?: throw IOException("Unexcepted Exception in ReadMinecraftHandShake")
		val nextProtocol = protocolSelector(connection, handshake)
		
	}
	
	companion object {
		fun readMinecraftHandshake(i: SizeLimitedInputStream): MinecraftHandshakePacket? {
			i.markWith(270) {
				if (i.available() < 5) return null
				val id = i.readByte()
				if (id != 0.toByte()) return null
				val len = i.readVarInt()
				if (i.available() < len + 3) return null
				val host = i.readString(len)
				val port = i.readUShort()
				val nextState = i.readVarInt()
				return MinecraftHandshakePacket(host, port, nextState)
			}
		}
	}
}


class MinecraftHandshakePacket(val host: String, val port: Int, val nextState: Int)