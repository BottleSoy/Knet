package top.soy_bottle.knet.utils

import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset

@Synchronized
@Throws(IOException::class)
fun <O : OutputStream> O.writeByte(byte: Byte): O {
	this.write(byte.toInt())
	return this
}

@Synchronized
@Throws(IOException::class)
fun <O : OutputStream> O.writeShort(short: Short): O {
	this.write(short.toInt() ushr 8)
	this.write(short.toInt())
	return this
}

@Synchronized
@Throws(IOException::class)
fun <O : OutputStream> O.writeVarInt(v: Int): O {
	var value = v
	while (true) {
		if (value and -0x80 == 0) {
			this.write(value)
			return this
		}
		this.write(value and 0x7F or 0x80)
		value = value ushr 7
	}
}

@Synchronized
@Throws(IOException::class)
fun <O : OutputStream> O.writeInt(int: Int): O {
	this.write(int ushr 24)
	this.write(int ushr 16)
	this.write(int ushr 8)
	this.write(int)
	return this
}

@Synchronized
@Throws(IOException::class)
fun <O : OutputStream> O.writeLong(long: Long): O {
	this.write((long ushr 56).toInt())
	this.write((long ushr 48).toInt())
	this.write((long ushr 40).toInt())
	this.write((long ushr 32).toInt())
	
	this.write((long ushr 24).toInt())
	this.write((long ushr 16).toInt())
	this.write((long ushr 8).toInt())
	this.write(long.toInt())
	return this
}

@Synchronized
@Throws(IOException::class)
fun <O : OutputStream> O.writeBoolean(bool: Boolean): O {
	write(if (bool) 1 else 0)
	return this
}

@Synchronized
@Throws(IOException::class)
fun <O : OutputStream> O.writeByteArray(byteArray: ByteArray): O {
	writeVarInt(byteArray.size)
	writeByteArray(byteArray, byteArray.size)
	return this
}

@Synchronized
@Throws(IOException::class)
fun <O : OutputStream> O.writeByteArray(byteArray: ByteArray, len: Int): O {
	if (byteArray.isEmpty()) return this
	this.write(byteArray, 0, len)
	return this
}

@Synchronized
@Throws(IOException::class)
fun <O : OutputStream> O.writeString(string: String, c: Charset = charset): O {
	val bytes = c.encode(string).array()
	val len = bytes.size
	this.writeVarInt(len)
	this.writeByteArray(bytes, len)
	return this
}

@Synchronized
@Throws(IOException::class)
fun <O : OutputStream> O.writeCharSequence(seq: CharSequence, c: Charset = charset): O {
	val bytes = c.encode(seq.toString()).array()
	val len = bytes.size
	this.writeByteArray(bytes, len)
	return this
}

@Synchronized
@Throws(IOException::class)
fun <O : OutputStream> O.writeString(string: String, len: Int, c: Charset = charset): O {
	val bytes = c.encode(string).array()
	if (bytes.size != len) throw IOException("size mismatch:${bytes.size} of $len")
	
	this.writeByteArray(bytes, len)
	return this
}

@Synchronized
@Throws(IOException::class)
fun <T, O : OutputStream> O.writeArray(array: List<T>, elementWrite: (OutputStream, T) -> OutputStream): O {
	this.writeVarInt(array.size)
	array.forEach { elementWrite(this, it) }
	return this
}

@Synchronized
@Throws(IOException::class)
fun <O : OutputStream> O.writeStringList(array: List<String>): O {
	this.writeVarInt(array.size)
	array.forEach { writeString(it) }
	return this
}