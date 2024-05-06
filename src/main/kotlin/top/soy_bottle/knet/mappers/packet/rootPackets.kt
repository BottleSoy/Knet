package top.soy_bottle.knet.mappers.packet

import top.soy_bottle.knet.utils.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable
import java.util.zip.Deflater
import java.util.zip.Inflater

@Env(Scope.Client)
class ClientMappers(
	val msgs: List<MapperMsg>,
) : Packet {
	data class MapperMsg(val id: Int, val type: String, val data: ByteArray) : Serializable
	
	constructor(i: InputStream) : this(
		msgs = i.readArray {
			MapperMsg(it.readInt(), it.readString(), it.readByteArray())
		}
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeArray(msgs) { outputStream, mapperMsg ->
			outputStream.writeInt(mapperMsg.id)
				.writeString(mapperMsg.type)
				.writeByteArray(mapperMsg.data)
		}
}

@Env(Scope.Server)
class ServerHi() {

}

@Env(Scope.Server)
class ServerMapResult(
	val id: Int,
	val code: Int,
	val time: Int,
	val errorMsg: String,
) : Packet {
	constructor(i: InputStream) : this(
		id = i.readInt(),
		code = i.readInt(),
		time = i.readInt(),
		errorMsg = i.readString()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeInt(id)
			.writeInt(code)
			.writeInt(time)
			.writeString(errorMsg)
	
}

@Env(Scope.Client)
class ClientPing(
	val id: Int,
	val clientTime: Long,
) : Packet {
	constructor(i: InputStream) : this(
		id = i.readInt(),
		clientTime = i.readLong()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeInt(id)
			.writeLong(clientTime)
}

@Env(Scope.Server)
class ServerPong(
	val id: Int,
	val serverTime: Long,
) : Packet {
	constructor(i: InputStream) : this(
		id = i.readInt(),
		serverTime = i.readLong()
	)
	
	override fun <O : OutputStream> write(out: O) =
		out.writeInt(id)
			.writeLong(serverTime)
}
@Env(Scope.Both)
class ZipPacket(
	val data: ByteArray,
) : Packet {
	constructor(i: InputStream) : this(decompress(i))
	
	companion object {
		fun decompress(i: InputStream): ByteArray {
			val bytes = i.readByteArray()
			val inflater = Inflater()
			inflater.setInput(bytes)
			val outputStream = ByteArrayOutputStream(1024)
			val buffer = ByteArray(1024)
			while (!inflater.finished()) {
				val count = inflater.inflate(buffer)
				outputStream.write(buffer, 0, count)
			}
			return outputStream.toByteArray()
		}
	}
	
	override fun <O : OutputStream> write(out: O): O {
		val deflater = Deflater()
		deflater.setInput(data)
		deflater.finish()
		val buffer = ByteArray(1024)
		val byteOut = ByteArrayOutputStream(data.size / 2)
		while (!deflater.finished()) {
			val count = deflater.deflate(buffer)
			byteOut.write(buffer, 0, count)
		}
		out.writeByteArray(byteOut.toByteArray())
		return out
	}
	
}
