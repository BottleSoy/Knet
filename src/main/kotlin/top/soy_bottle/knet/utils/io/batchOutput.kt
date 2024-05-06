package top.soy_bottle.knet.utils.io

import java.io.OutputStream

fun newOutputStream(outputHandler: (b: ByteArray, off: Int, len: Int) -> Unit) = object : OutputStream() {
	override fun write(b: Int) {
		write(ByteArray(1) { b.toByte() })
	}
	
	override fun write(b: ByteArray, off: Int, len: Int) {
		outputHandler(b, off, len)
	}
}