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

/**
 *
 */
class ClientNewTcpConnectionPacket(
	val connectionId: Int,
) : TcpClientPacket {
	constructor(i: InputStream) : this(
		i.readInt()
	)
	override fun <O : OutputStream> write(out: O) =
		out.writeInt(connectionId)
	
}


class ServerNewTcpConnectionPacket(
	val mapperId: Int,
	val connectionId: Int,
	val info: ConnectionInfo,
) : TcpServerPacket {
	constructor(i: InputStream) : this(
		i.readInt(),
		i.readInt(),
		i.readConnectionInfo()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeInt(mapperId)
			.writeInt(connectionId)
			.writeConnectionInfo(info)
}

class ClientTcpDataPacket(
	val connectionId: Int,
	val data: ByteArray,
	val len: Int = data.size,
) : TcpClientPacket {
	constructor(i: InputStream) : this(
		i.readInt(),
		i.readByteArray()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeInt(connectionId)
			.writeInt(len)
			.writeByteArray(data, len)
	
}

class ServerTcpDataPacket(
	val connectionId: Int,
	val data: ByteArray,
	val len: Int = data.size,
) : TcpServerPacket {
	constructor(buf: InputStream) : this(
		buf.readInt(),
		buf.readByteArray()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeInt(connectionId)
			.writeInt(len)
			.writeByteArray(data, len)
	
}

class ClientTcpClosePacket(
	val connectionId: Int,
	val normalClose: Boolean,
) : TcpClientPacket {
	constructor(buf: InputStream) : this(
		buf.readInt(),
		buf.readBoolean()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeInt(connectionId)
			.writeBoolean(normalClose)
	
}

class ServerTcpClosePacket(
	val connectionId: Int,
	val normalClose: Boolean,
) : TcpServerPacket {
	constructor(buf: InputStream) : this(
		buf.readInt(),
		buf.readBoolean()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeInt(connectionId)
			.writeBoolean(normalClose)
}
