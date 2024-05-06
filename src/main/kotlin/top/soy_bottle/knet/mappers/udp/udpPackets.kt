package top.soy_bottle.knet.mappers.udp

import top.soy_bottle.knet.address.Address
import top.soy_bottle.knet.address.readAddress
import top.soy_bottle.knet.address.writeAddress
import top.soy_bottle.knet.mappers.packet.Packet
import top.soy_bottle.knet.utils.readByteArray
import top.soy_bottle.knet.utils.readInt
import top.soy_bottle.knet.utils.writeByteArray
import top.soy_bottle.knet.utils.writeInt
import java.io.InputStream
import java.io.OutputStream

/**
 * C->S数据包
 */
class UdpClientPacket(
	val sourceAddress: Address,
	val id: Int,
	val data: ByteArray,
	val len: Int = data.size,
) : Packet {
	constructor(i: InputStream) : this(
		i.readAddress(),
		i.readInt(),
		i.readByteArray()
	)
	
	override fun <O : OutputStream> write(out: O) = out
		.writeAddress(sourceAddress)
		.writeInt(id)
		.writeInt(len)
		.writeByteArray(data, len)
}

/**
 * S->C数据包
 */
class UdpServerPacket(
	val sourceAddress: Address,
	val id: Int,
	val data: ByteArray,
	val len: Int = data.size,
) : Packet {
	constructor(i: InputStream) : this(
		i.readAddress(),
		i.readInt(),
		i.readByteArray()
	)
	
	override fun <O : OutputStream> write(out: O) = out
		.writeAddress(sourceAddress)
		.writeInt(id)
		.writeInt(len)
		.writeByteArray(data, len)
}


