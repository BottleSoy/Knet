package top.soy_bottle.knet.mappers.tcp

import top.soy_bottle.knet.protocols.ConnectionInfo
import top.soy_bottle.knet.mappers.packet.Packet
import top.soy_bottle.knet.protocols.readConnectionInfo
import top.soy_bottle.knet.protocols.writeConnectionInfo
import top.soy_bottle.knet.utils.*
import java.io.InputStream
import java.io.OutputStream

/**
 * C->S数据包
 */
sealed interface TcpClientPacket : Packet

/**
 * S->C数据包
 */
sealed interface TcpServerPacket : Packet

class ServerNewTcpConnectionPacket(
	val mapperId: Int,
	val connectionId: Int,
	val info: ConnectionInfo,
) : TcpServerPacket {
	constructor(i: InputStream) : this(
		i.readVarInt(),
		i.readVarInt(),
		i.readConnectionInfo()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeVarInt(mapperId)
			.writeVarInt(connectionId)
			.writeConnectionInfo(info)
}

class ClientTcpDataPacket(
	val connectionId: Int,
	val data: ByteArray,
	val len: Int = data.size,
) : TcpClientPacket {
	constructor(buf: InputStream) : this(
		buf.readVarInt(),
		buf.readByteArray()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeVarInt(connectionId)
			.writeByteArray(data, len)
	
}

class ServerTcpDataPacket(
	val connectionId: Int,
	val data: ByteArray,
	val len: Int = data.size
) : TcpServerPacket {
	constructor(buf: InputStream) : this(
		buf.readVarInt(),
		buf.readByteArray()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeVarInt(connectionId)
			.writeByteArray(data, len)
	
}

class ClientTcpClosePacket(
	val connectionId: Int,
	val normalClose: Boolean,
) : TcpClientPacket {
	constructor(buf: InputStream) : this(
		buf.readVarInt(),
		buf.readBoolean()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeVarInt(connectionId)
			.writeBoolean(normalClose)
	
}

class ServerTcpClosePacket(
	val connectionId: Int,
	val normalClose: Boolean,
) : TcpServerPacket {
	constructor(buf: InputStream) : this(
		buf.readVarInt(),
		buf.readBoolean()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeVarInt(connectionId)
			.writeBoolean(normalClose)
}
