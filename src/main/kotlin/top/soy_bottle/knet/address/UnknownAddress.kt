package top.soy_bottle.knet.address

import java.io.OutputStream

object UnknownAddress : Address() {
	override val ipString: String = ""
	override val port: Int = 0
	override val type: AddressType= AddressType.UNKNOWN
	
	override fun <O : OutputStream> writeAddress(out: O) = out
	
	override fun hashCode(): Int {
		TODO("Not yet implemented")
	}
	
	override fun equals(other: Any?): Boolean {
		TODO("Not yet implemented")
	}
}