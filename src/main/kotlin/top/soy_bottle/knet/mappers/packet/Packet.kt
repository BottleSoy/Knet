package top.soy_bottle.knet.mappers.packet

import java.io.OutputStream


interface Packet {
	fun <O : OutputStream> write(out: O): O
}