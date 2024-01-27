package top.soy_bottle.knet.utils

import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

fun InputStream.readRaw(size: Int = 2048): Pair<Int, ByteArray> {
	val bytes = ByteArray(size)
	val readed = this.read(bytes, 0, size)
	return readed to bytes
}

@Synchronized
@Throws(IOException::class)
fun InputStream.readByte(): Byte {
	return this.read().toByte()
}

@Synchronized
@Throws(IOException::class)
fun InputStream.readUByte(): Int {
	return this.read()
}

@Synchronized
@Throws(IOException::class)
fun InputStream.readShort(): Short {
	return ((readByte().toInt() and 0xff shl 8) or
		(readByte().toInt() and 0xff)).toShort()
}

@Synchronized
@Throws(IOException::class)
fun InputStream.readUShort(): Int {
	return ((readByte().toInt() and 0xff shl 8) or
		(readByte().toInt() and 0xff))
}

@Synchronized
@Throws(IOException::class)
fun InputStream.readVarInt(): Int {
	var b: Byte
	var i = 0
	var j = 0
	do {
		b = this.readByte()
		i = i or (b.toInt() and 0x7F shl j++) * 7
		if (j <= 5) continue
		throw RuntimeException("VarInt too big")
	} while (b.toInt() and 0x80 == 128)
	return i
}

@Synchronized
@Throws(IOException::class)
fun InputStream.readInt(): Int {
	return (readByte().toInt() and 0xff shl 24) or
		(readByte().toInt() and 0xff shl 16) or
		(readByte().toInt() and 0xff shl 8) or
		(readByte().toInt() and 0xff)
}

@Synchronized
@Throws(IOException::class)
fun InputStream.readLong(): Long {
	return (readByte().toLong() and 0xff shl 56) or
		(readByte().toLong() and 0xff shl 48) or
		(readByte().toLong() and 0xff shl 40) or
		(readByte().toLong() and 0xff shl 32) or
		
		(readByte().toLong() and 0xff shl 24) or
		(readByte().toLong() and 0xff shl 16) or
		(readByte().toLong() and 0xff shl 8) or
		(readByte().toLong() and 0xff)
}

@Synchronized
@Throws(IOException::class)
fun InputStream.readBoolean() = read() == 0

@Synchronized
@Throws(IOException::class)
fun InputStream.readByteArray() = readByteArray(readVarInt())

@Synchronized
@Throws(IOException::class)
fun InputStream.readByteArray(len: Int): ByteArray = readNBytes(len)

fun ByteArray.javaBuffer(): ByteBuffer = ByteBuffer.wrap(this)

@Synchronized
@Throws(IOException::class)
fun InputStream.readString() = readString(readVarInt())

@Synchronized
@Throws(IOException::class)
fun InputStream.readString(len: Int): String {
	val bytes = readByteArray(len)
	return charset.decode(bytes.javaBuffer()).toString()
}


@Synchronized
@Throws(IOException::class)
fun <T> InputStream.readArray(elementRead: (InputStream) -> T): List<T> {
	val size = readVarInt()
	val list = ArrayList<T>(size)
	repeat(size) {
		list += elementRead(this)
	}
	return list
}

@Synchronized
@Throws(IOException::class)
fun InputStream.readStringList(): List<String> = readArray { readString() }
