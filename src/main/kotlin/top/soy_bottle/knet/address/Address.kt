package top.soy_bottle.knet.address

import top.soy_bottle.knet.utils.readInt
import top.soy_bottle.knet.utils.writeInt
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable

sealed class Address : Serializable {
	abstract val ipString: String
	abstract val port: Int
	abstract val type: AddressType
	
	abstract fun <O : OutputStream> writeAddress(out: O): O
	abstract override fun hashCode(): Int
	abstract override fun equals(other: Any?): Boolean
}

fun <O : OutputStream> O.writeAddress(address: Address): O {
	this.writeInt(address.type.ordinal)
	address.writeAddress(this)
	return this
}

fun InputStream.readAddress(): Address {
	val type = this.readInt()
	return AddressType.values()[type].parser(this)
}