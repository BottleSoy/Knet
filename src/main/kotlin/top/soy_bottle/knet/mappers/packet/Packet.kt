package top.soy_bottle.knet.mappers.packet

import java.io.OutputStream
import java.io.Serializable


interface Packet : Serializable {
	fun <O : OutputStream> write(out: O): O
}