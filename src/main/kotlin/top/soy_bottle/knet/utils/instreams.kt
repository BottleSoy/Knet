package top.soy_bottle.knet.utils

import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

@Throws(IOException::class)
fun InputStream.readByte(): Byte = synchronized(this) {
	return this.read().toByte()
}

@Throws(IOException::class)
fun InputStream.readUByte(): Int = synchronized(this) {
	return this.read()
}

@Throws(IOException::class)
fun InputStream.readShort(): Short = synchronized(this) {
	return ((readByte().toInt() and 0xff shl 8) or
		(readByte().toInt() and 0xff)).toShort()
}

@Throws(IOException::class)
fun InputStream.readUShort(): Int = synchronized(this) {
	return ((readByte().toInt() and 0xff shl 8) or
		(readByte().toInt() and 0xff))
}


@Throws(IOException::class)
fun InputStream.readInt(): Int = synchronized(this) {
	return (readByte().toInt() and 0xff shl 24) or
		(readByte().toInt() and 0xff shl 16) or
		(readByte().toInt() and 0xff shl 8) or
		(readByte().toInt() and 0xff)
}

@Throws(IOException::class)
fun InputStream.readFloat(): Float = synchronized(this) {
	return java.lang.Float.intBitsToFloat(this.readInt())
}


@Throws(IOException::class)
fun InputStream.readLong(): Long = synchronized(this) {
	return (readByte().toLong() and 0xff shl 56) or
		(readByte().toLong() and 0xff shl 48) or
		(readByte().toLong() and 0xff shl 40) or
		(readByte().toLong() and 0xff shl 32) or
		
		(readByte().toLong() and 0xff shl 24) or
		(readByte().toLong() and 0xff shl 16) or
		(readByte().toLong() and 0xff shl 8) or
		(readByte().toLong() and 0xff)
}


@Throws(IOException::class)
fun InputStream.readDouble(): Double = synchronized(this) { java.lang.Double.longBitsToDouble(readLong()) }

@Throws(IOException::class)
fun InputStream.readBoolean() = synchronized(this) { read() == 0 }

@Throws(IOException::class)
fun InputStream.readByteArray() = synchronized(this) { readByteArray(this.readInt()) }


@Throws(IOException::class)
fun InputStream.readByteArray(len: Int): ByteArray = synchronized(this) {
	val data = ByteArray(len)
	var pos = 0
	while (pos < len) {
		val remain = len - pos
		val c = read(data, pos, remain)
		pos += c
	}
	return data
}

fun ByteArray.javaBuffer(): ByteBuffer = ByteBuffer.wrap(this)

@Throws(IOException::class)
fun InputStream.readString() = synchronized(this) {
	readString(this.readInt())
}

@Throws(IOException::class)
fun InputStream.readString(len: Int): String = synchronized(this) {
	val bytes = readByteArray(len)
	return charset.decode(bytes.javaBuffer()).toString()
}


@Throws(IOException::class)
fun <T> InputStream.readArray(elementRead: (InputStream) -> T): List<T> = synchronized(this) {
	val size = this.readInt()
	val list = ArrayList<T>(size)
	repeat(size) {
		list += elementRead(this)
	}
	return list
}

@Throws(IOException::class)
fun InputStream.readStringList(): List<String> = synchronized(this) { readArray { readString() } }
