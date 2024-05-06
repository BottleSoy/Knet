package top.soy_bottle.knet.address

import top.soy_bottle.knet.utils.*
import java.io.InputStream
import java.io.OutputStream
import java.net.Inet4Address

class IPv4Address(
	tcp: Boolean,
	val address: ByteArray,
	override val port: Int,
) : Address() {
	constructor(tcp: Boolean, address: Inet4Address, port: Int) : this(tcp, address.address, port)
	
	override val ipString: String
		get() = addressToString(address)
	
	override val type: AddressType = if (tcp) AddressType.IPV4_TCP else AddressType.IPV4_UDP
	override fun <O : OutputStream> writeAddress(out: O) =
		out.writeByteArray(address, 4)
			.writeInt(port)
	
	
	override fun toString() = "[${type.typeName}] ${addressToString(address)}:$port"
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		
		other as IPv4Address
		
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
		fun parseAddress4(type: AddressType, `in`: InputStream): IPv4Address {
			val address = `in`.readByteArray(4)
			val port = `in`.readInt()
			return IPv4Address(type == AddressType.IPV4_TCP, address, port)
		}
		
		fun addressToString(address: ByteArray): String {
			return (address[0].toInt() and 0xff).toString() + "." + (address[1].toInt() and 0xff) + "." + (address[2].toInt() and 0xff) + "." + (address[3].toInt() and 0xff)
		}
	}
}