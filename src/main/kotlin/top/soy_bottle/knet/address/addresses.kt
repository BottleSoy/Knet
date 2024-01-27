package top.soy_bottle.knet.address

import top.soy_bottle.knet.utils.readVarInt
import java.io.InputStream
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.UnixDomainSocketAddress

/**
 * 字符串转换成Socket地址
 */
fun String.toAddress(): SocketAddress =
	if (this.contains('/') || this.contains('\\')) { //unix domain socket
		UnixDomainSocketAddress.of(this)
	} else if (!this.contains(':')) {
		InetSocketAddress(this.toInt())
	} else {
		val (hostname, port) = this.split(':')
		InetSocketAddress(hostname, port.toInt())
	}

fun InputStream.parseAddress(): Address {
	val type = AddressType.values()[this.readVarInt()]
	return type.parser(this);
}

fun SocketAddress.toAddress(isTcp: Boolean): Address = when (this) {
	is InetSocketAddress -> {
		when (val addr = this.address) {
			is Inet4Address -> IPv4Address(isTcp, addr, this.port)
			is Inet6Address -> IPv6Address(isTcp, addr, this.port)
			else -> throw IllegalArgumentException()
		}
	}
	is UnixDomainSocketAddress -> UnixDomainAddress(this.path.toFile().path)
	
	else -> throw IllegalArgumentException()
}