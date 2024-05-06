package top.soy_bottle.knet.utils

import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset

@Throws(IOException::class)
fun <O : OutputStream> O.writeByte(byte: Byte): O = synchronized(this) {
	this.write(byte.toInt())
	return this
}

@Throws(IOException::class)
fun <O : OutputStream> O.writeShort(short: Short): O = synchronized(this) {
	this.write(short.toInt() ushr 8)
	this.write(short.toInt())
	return this
}

@Throws(IOException::class)
fun <O : OutputStream> O.writeInt(int: Int): O = synchronized(this) {
	this.write(int ushr 24)
	this.write(int ushr 16)
	this.write(int ushr 8)
	this.write(int)
	return this
}

@Throws(IOException::class)
fun <O : OutputStream> O.writeLong(long: Long): O = synchronized(this) {
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

@Throws(IOException::class)
fun <O : OutputStream> O.writeBoolean(bool: Boolean): O = synchronized(this) {
	write(if (bool) 1 else 0)
	return this
}

@Throws(IOException::class)
fun <O : OutputStream> O.writeByteArray(byteArray: ByteArray): O = synchronized(this) {
	this.writeInt(byteArray.size)
	writeByteArray(byteArray, byteArray.size)
	return this
}

@Throws(IOException::class)
fun <O : OutputStream> O.writeByteArray(byteArray: ByteArray, len: Int): O = synchronized(this) {
	if (byteArray.isEmpty()) return this
	this.write(byteArray, 0, len)
	return this
}

@Throws(IOException::class)
fun <O : OutputStream> O.writeString(string: String, c: Charset = Charsets.UTF_8): O = synchronized(this) {
	val bytes = c.encode(string)
	val len = bytes.limit()
	this.writeInt(len)
	this.writeByteArray(bytes.array(), len)
	return this
}

@Throws(IOException::class)
fun <O : OutputStream> O.writeCharSequence(seq: CharSequence, c: Charset = Charsets.UTF_8): O = synchronized(this) {
	val bytes = c.encode(seq.toString()).array()
	val len = bytes.size
	this.writeByteArray(bytes, len)
	return this
}

@Throws(IOException::class)
fun <O : OutputStream> O.writeString(string: String, len: Int, c: Charset = Charsets.UTF_8): O = synchronized(this) {
	val bytes = c.encode(string).array()
	if (bytes.size != len) throw IOException("size mismatch:${bytes.size} of $len")
	
	this.writeByteArray(bytes, len)
	return this
}

@Throws(IOException::class)
fun <T, O : OutputStream> O.writeArray(array: List<T>, elementWrite: (OutputStream, T) -> OutputStream): O = synchronized(this) {
	this.writeInt(array.size)
	array.forEach { elementWrite(this, it) }
	return this
}

@Throws(IOException::class)
fun <O : OutputStream> O.writeStringList(array: List<String>): O = synchronized(this) {
	this.writeInt(array.size)
	array.forEach { writeString(it) }
	return this
}

@Throws(IOException::class)
fun <O : OutputStream> O.writeDouble(data: Double): O = synchronized(this) {
	this.writeLong(java.lang.Double.doubleToLongBits(data))
	return this
}

@Throws(IOException::class)
fun <O : OutputStream> O.writeFloat(data: Float): O = synchronized(this) {
	this.writeInt(java.lang.Float.floatToIntBits(data))
	return this
}
