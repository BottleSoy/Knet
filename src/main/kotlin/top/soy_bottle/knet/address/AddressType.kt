package top.soy_bottle.knet.address

import java.io.InputStream

enum class AddressType(
	val parser: (InputStream) -> Address,
	val typeName: String,
	val addressType: String,
) {
	IPV4_TCP({ input -> IPv4Address.parseAddress4(IPV4_TCP, input) }, "TCP", "IPV4"),
	IPV4_UDP({ input -> IPv4Address.parseAddress4(IPV4_UDP, input) }, "UDP", "IPV4"),
	IPV6_TCP({ input -> IPv6Address.parseAddress6(IPV6_TCP, input) }, "TCP", "IPV6"),
	IPV6_UDP({ input -> IPv6Address.parseAddress6(IPV6_UDP, input) }, "UDP", "IPV6"),
	UNIX_DOMAIN_SOCKET(UnixDomainAddress::parse, "UDS", "NONE");
	
}
