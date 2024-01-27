package top.soy_bottle.knet.address

import top.soy_bottle.knet.utils.readString
import top.soy_bottle.knet.utils.writeString
import java.io.InputStream
import java.io.OutputStream

class UnixDomainAddress(
	val path: String,
) : Address() {
	override val ipString: String
		get() = path
	override val port = -1
	override val type: AddressType = AddressType.UNIX_DOMAIN_SOCKET
	
	override fun <O : OutputStream> writeAddress(out: O) =
		out.writeString(path)
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		
		other as UnixDomainAddress
		
		return path == other.path
	}
	
	override fun hashCode(): Int {
		return path.hashCode()
	}
	
	companion object {
		fun parse(i: InputStream) = UnixDomainAddress(i.readString())
		
	}
}