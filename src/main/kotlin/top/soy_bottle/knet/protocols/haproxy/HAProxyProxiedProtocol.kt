/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package top.soy_bottle.knet.protocols.haproxy

/**
 * A protocol proxied by HAProxy which is represented by its transport protocol and address family.
 */
enum class HAProxyProxiedProtocol(
	val byteValue: Byte,
	val addressFamily: AddressFamily,
	val transportProtocol: TransportProtocol,
) {
	/**
	 * The UNKNOWN represents a connection which was forwarded for an unknown protocol and an unknown address family.
	 */
	UNKNOWN(HAProxyConstants.TPAF_UNKNOWN_BYTE, AddressFamily.AF_UNSPEC, TransportProtocol.UNSPEC),
	
	/**
	 * The TCP4 represents a connection which was forwarded for an IPv4 client over TCP.
	 */
	TCP4(HAProxyConstants.TPAF_TCP4_BYTE, AddressFamily.AF_IPv4, TransportProtocol.STREAM),
	
	/**
	 * The TCP6 represents a connection which was forwarded for an IPv6 client over TCP.
	 */
	TCP6(HAProxyConstants.TPAF_TCP6_BYTE, AddressFamily.AF_IPv6, TransportProtocol.STREAM),
	
	/**
	 * The UDP4 represents a connection which was forwarded for an IPv4 client over UDP.
	 */
	UDP4(HAProxyConstants.TPAF_UDP4_BYTE, AddressFamily.AF_IPv4, TransportProtocol.DGRAM),
	
	/**
	 * The UDP6 represents a connection which was forwarded for an IPv6 client over UDP.
	 */
	UDP6(HAProxyConstants.TPAF_UDP6_BYTE, AddressFamily.AF_IPv6, TransportProtocol.DGRAM),
	
	/**
	 * The UNIX_STREAM represents a connection which was forwarded for a UNIX stream socket.
	 */
	UNIX_STREAM(HAProxyConstants.TPAF_UNIX_STREAM_BYTE, AddressFamily.AF_UNIX, TransportProtocol.STREAM),
	
	/**
	 * The UNIX_DGRAM represents a connection which was forwarded for a UNIX datagram socket.
	 */
	UNIX_DGRAM(HAProxyConstants.TPAF_UNIX_DGRAM_BYTE, AddressFamily.AF_UNIX, TransportProtocol.DGRAM);
	
	
	companion object {
		/**
		 * Returns the [HAProxyProxiedProtocol] represented by the specified byte.
		 *
		 * @param tpafByte transport protocol and address family byte
		 */
		fun valueOf(tpafByte: Byte): HAProxyProxiedProtocol {
			return when (tpafByte) {
				HAProxyConstants.TPAF_TCP4_BYTE -> TCP4
				HAProxyConstants.TPAF_TCP6_BYTE -> TCP6
				HAProxyConstants.TPAF_UNKNOWN_BYTE -> UNKNOWN
				HAProxyConstants.TPAF_UDP4_BYTE -> UDP4
				HAProxyConstants.TPAF_UDP6_BYTE -> UDP6
				HAProxyConstants.TPAF_UNIX_STREAM_BYTE -> UNIX_STREAM
				HAProxyConstants.TPAF_UNIX_DGRAM_BYTE -> UNIX_DGRAM
				else -> throw IllegalArgumentException(
					"unknown transport protocol + address family: " + (tpafByte.toInt() and 0xFF)
				)
			}
		}
	}
}

/**
 * The address family of an HAProxy proxy protocol header.
 */
enum class AddressFamily
/**
 * Creates a new instance
 */(private val byteValue: Byte) {
	/**
	 * The UNSPECIFIED address family represents a connection which was forwarded for an unknown protocol.
	 */
	AF_UNSPEC(HAProxyConstants.AF_UNSPEC_BYTE),
	
	/**
	 * The IPV4 address family represents a connection which was forwarded for an IPV4 client.
	 */
	AF_IPv4(HAProxyConstants.AF_IPV4_BYTE),
	
	/**
	 * The IPV6 address family represents a connection which was forwarded for an IPV6 client.
	 */
	AF_IPv6(HAProxyConstants.AF_IPV6_BYTE),
	
	/**
	 * The UNIX address family represents a connection which was forwarded for a unix socket.
	 */
	AF_UNIX(HAProxyConstants.AF_UNIX_BYTE);
	
	/**
	 * Returns the byte value of this address family.
	 */
	fun byteValue(): Byte {
		return byteValue
	}
	
	companion object {
		/**
		 * The highest 4 bits of the transport protocol and address family byte contain the address family
		 */
		private const val FAMILY_MASK = 0xf0.toByte()
		
		/**
		 * Returns the [AddressFamily] represented by the highest 4 bits of the specified byte.
		 *
		 * @param tpafByte transport protocol and address family byte
		 */
		fun valueOf(tpafByte: Byte): AddressFamily {
			val addressFamily = tpafByte.toInt() and FAMILY_MASK.toInt()
			return when (addressFamily.toByte()) {
				HAProxyConstants.AF_IPV4_BYTE -> AF_IPv4
				HAProxyConstants.AF_IPV6_BYTE -> AF_IPv6
				HAProxyConstants.AF_UNSPEC_BYTE -> AF_UNSPEC
				HAProxyConstants.AF_UNIX_BYTE -> AF_UNIX
				else -> throw IllegalArgumentException("unknown address family: $addressFamily")
			}
		}
	}
}

/**
 * The transport protocol of an HAProxy proxy protocol header
 */
enum class TransportProtocol
/**
 * Creates a new instance.
 */(private val transportByte: Byte) {
	/**
	 * The UNSPEC transport protocol represents a connection which was forwarded for an unknown protocol.
	 */
	UNSPEC(HAProxyConstants.TRANSPORT_UNSPEC_BYTE),
	
	/**
	 * The STREAM transport protocol represents a connection which was forwarded for a TCP connection.
	 */
	STREAM(HAProxyConstants.TRANSPORT_STREAM_BYTE),
	
	/**
	 * The DGRAM transport protocol represents a connection which was forwarded for a UDP connection.
	 */
	DGRAM(HAProxyConstants.TRANSPORT_DGRAM_BYTE);
	
	/**
	 * Returns the byte value of this transport protocol.
	 */
	fun byteValue(): Byte {
		return transportByte
	}
	
	companion object {
		/**
		 * The transport protocol is specified in the lowest 4 bits of the transport protocol and address family byte
		 */
		private const val TRANSPORT_MASK: Byte = 0x0f
		
		/**
		 * Returns the [TransportProtocol] represented by the lowest 4 bits of the specified byte.
		 *
		 * @param tpafByte transport protocol and address family byte
		 */
		fun valueOf(tpafByte: Byte): TransportProtocol {
			val transportProtocol = tpafByte.toInt() and TRANSPORT_MASK.toInt()
			return when (transportProtocol.toByte()) {
				HAProxyConstants.TRANSPORT_STREAM_BYTE -> STREAM
				HAProxyConstants.TRANSPORT_UNSPEC_BYTE -> UNSPEC
				HAProxyConstants.TRANSPORT_DGRAM_BYTE -> DGRAM
				else -> throw IllegalArgumentException("unknown transport protocol: $transportProtocol")
			}
		}
	}
}