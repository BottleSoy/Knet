package top.soy_bottle.knet.utils

import java.nio.charset.Charset

val charset: Charset = Charsets.UTF_8

fun ByteArray.subArray(off: Int, len: Int): ByteArray {
	val newArray = ByteArray(len)
	System.arraycopy(this, off, newArray, 0, len)
	return newArray
}