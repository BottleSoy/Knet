package top.soy_bottle.knet.utils

import java.nio.charset.Charset

val charset: Charset = Charsets.UTF_8
//
//fun ByteBuf.writeVarInt(v: Int): ByteBuf {
//	var value = v
//	while (true) {
//		if (value and -0x80 == 0) {
//			this.writeByte(value)
//			return this
//		}
//		this.writeByte(value and 0x7F or 0x80)
//		value = value ushr 7
//	}
//}
//
//fun ByteBuf.readVarInt(): Int {
//	var b: Byte
//	var i = 0
//	var j = 0
//	do {
//		b = this.readByte()
//		i = i or (b.toInt() and 0x7F shl j++) * 7
//		if (j <= 5) continue
//		throw RuntimeException("VarInt too big")
//	} while (b.toInt() and 0x80 == 128)
//	return i
//}
//
//fun ByteBuf.writeString(string: String): ByteBuf {
//	this.writeVarInt(string.length)
//	if (string.isNotEmpty())
//		this.writeCharSequence(string, charset)
//	return this
//}
//
//fun ByteBuf.readString(): String {
//	val l = this.readVarInt()
//	if (l == 0) return ""
//	return this.readCharSequence(l, charset).toString()
//}
//
//fun ByteBuf.writeByteArray(length: Int, array: ByteArray): ByteBuf {
//	repeat(length) {
//		this.writeByte(array[it].toInt())
//	}
//	return this
//}
//
//fun ByteBuf.readByteArray(length: Int): ByteArray {
//	return ByteArray(length) {
//		this.readByte()
//	}
//}
//
//fun ByteBuf.writeByteArray(array: ByteArray): ByteBuf {
//	val len = array.size
//	this.writeVarInt(len)
//	this.writeByteArray(len, array)
//	return this
//}
//
//fun ByteBuf.readByteArray() = readByteArray(readVarInt())
//
//fun <T> ByteBuf.writeArray(array: Array<T>, elementWrite: (ByteBuf, T) -> ByteBuf) {
//	this.writeVarInt(array.size)
//	array.forEach { elementWrite(this, it) }
//}
//
//inline fun <reified T> ByteBuf.readArray(elementRead: (ByteBuf) -> T): Array<T> {
//	return Array(readVarInt()) {
//		elementRead(this)
//	}
//}