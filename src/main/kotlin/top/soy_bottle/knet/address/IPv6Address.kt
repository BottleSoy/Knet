package top.soy_bottle.knet.address

import top.soy_bottle.knet.utils.*
import java.io.InputStream
import java.io.OutputStream
import java.net.Inet6Address

class IPv6Address(
	tcp: Boolean,
	val address: ByteArray,
	override val port: Int,
) : Address() {
	constructor(tcp: Boolean, address: Inet6Address, port: Int) : this(tcp, address.address, port)
	
	override val ipString: String
		get() = addressToString(address)
	
	override val type: AddressType = if (tcp) AddressType.IPV6_TCP else AddressType.IPV6_UDP
	
	override fun <O : OutputStream> writeAddress(out: O) =
		out.writeByteArray(address, INADDRSZ)
			.writeInt(port)
	
	
	override fun toString() = "[${type.typeName}] ${addressToString(address)}:$port"
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		
		other as IPv6Address
		
		if (!address.contentEquals(other.address)) return false
		if (port != other.port) return false
		if (type != other.type) return false
		
		return true
	}
	
	override fun hashCode(): Int {
		var result = address.contentHashCode()
		result = 31 * result + port
		result = 31 * result + type.hashCode()
		return result
	}
	
	companion object {
		fun parseAddress6(type: AddressType, i: InputStream): IPv6Address {
			val address = i.readNBytes(INADDRSZ) //128位地址
			val port = i.readInt()
			return IPv6Address(type == AddressType.IPV6_TCP, address, port)
		}
		
		// Utilities
		private const val INADDRSZ: Int = 16
		private const val INT16SZ = 2
		fun addressToString(src: ByteArray): String {
			val sb = StringBuilder(41)
			sb.append("[")
			for (i in 0 until INADDRSZ / INT16SZ) {
				sb.append(
					Integer.toHexString(
						src[i shl 1].toInt() shl 8 and 0xff00
							or (src[(i shl 1) + 1].toInt() and 0xff)
					)
				)
				if (i < (INADDRSZ / INT16SZ) - 1) {
					sb.append(":")
				}
			}
			return sb.append("]").toString()
		}
	}
}