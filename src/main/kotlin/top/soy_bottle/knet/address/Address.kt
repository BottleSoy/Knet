package top.soy_bottle.knet.address

import top.soy_bottle.knet.utils.readVarInt
import java.io.InputStream
import java.io.OutputStream

sealed class Address {
	abstract val ipString: String
	abstract val port: Int
	abstract val type: AddressType
	
	abstract fun <O : OutputStream> writeAddress(out: O): O
	abstract override fun hashCode(): Int
	abstract override fun equals(other: Any?): Boolean
}

fun <O : OutputStream> O.writeAddress(address: Address) = address.writeAddress(this)

fun InputStream.readAddress(): Address {
	val type = this.readVarInt()
	return AddressType.values()[type].parser(this)
}