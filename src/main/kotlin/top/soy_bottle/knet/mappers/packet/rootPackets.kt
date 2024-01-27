package top.soy_bottle.knet.mappers.packet

import top.soy_bottle.knet.utils.*
import java.io.InputStream
import java.io.OutputStream

@Env(Scope.Client)
class ClientMappers(
	val msgs: List<ByteArray>,
) : Packet {
	
	@Env(Scope.Server)
	constructor(i: InputStream) : this(
		msgs = i.readArray {
			it.readByteArray()
		}
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeArray(msgs) { buf, it ->
			buf.writeByteArray(it)
		}
}

@Env(Scope.Server)
class ServerMapResult(
	val id: Int,
	val code: Int,
	val time: Int,
	val errorMsg: String,
) : Packet {
	constructor(i: InputStream) : this(
		id = i.readVarInt(),
		code = i.readVarInt(),
		time = i.readVarInt(),
		errorMsg = i.readString()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeVarInt(id)
			.writeVarInt(code)
			.writeVarInt(time)
			.writeString(errorMsg)
	
}

@Env(Scope.Client)
class ClientPing(
	val id: Int,
	val clientTime: Long,
) : Packet {
	constructor(i: InputStream) : this(
		id = i.readVarInt(),
		clientTime = i.readLong()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeVarInt(id)
			.writeLong(clientTime)
}

@Env(Scope.Server)
class ServerPong(
	val id: Int,
	val serverTime: Long,
) : Packet {
	constructor(i: InputStream) : this(
		id = i.readVarInt(),
		serverTime = i.readLong()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeVarInt(id)
			.writeLong(serverTime)
}


