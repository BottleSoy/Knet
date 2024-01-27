/*
 * Copyright 2020 The Netty Project
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

import top.soy_bottle.knet.address.Address
import top.soy_bottle.knet.address.AddressType
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.protocols.haproxy.HAProxyConstants.V2_PREFIX
import top.soy_bottle.knet.protocols.haproxy.HAProxyConstants.V1_PREFIX
import top.soy_bottle.knet.utils.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.net.UnixDomainSocketAddress
import kotlin.io.path.absolutePathString

/**
 * Encodes an HAProxy proxy protocol message
 *
 * [Proxy Protocol Specification](https://www.haproxy.org/download/1.8/doc/proxy-protocol.txt)
 */
object HAProxyMessageEncoder {
	/**
	 * create HAProxyMessage
	 */
	fun createMessage(connection: Connection, version: HAProxyProtocolVersion = HAProxyProtocolVersion.V2): ByteArray {
		val rootSocket = connection.parents.first().javaSocket()
		val remoteAddress = rootSocket.remoteSocketAddress
		val localAddress = rootSocket.localSocketAddress
		
		val message = when (remoteAddress) {
			is InetSocketAddress -> {
				localAddress as InetSocketAddress
				when (remoteAddress.address) {
					is Inet4Address -> {
						HAProxyMessage(
							version, HAProxyCommand.PROXY, HAProxyProxiedProtocol.TCP4,
							remoteAddress.address.hostAddress, localAddress.address.hostAddress,
							rootSocket.port, rootSocket.localPort
						)
					}
					
					is Inet6Address -> {
						HAProxyMessage(
							version, HAProxyCommand.PROXY, HAProxyProxiedProtocol.TCP6,
							remoteAddress.address.hostAddress, localAddress.address.hostAddress,
							rootSocket.port, rootSocket.localPort
						)
					}
					
					else -> throw HAProxyProtocolException("Unkown Address of:${remoteAddress.address.javaClass.simpleName}")
				}
			}
			
			is UnixDomainSocketAddress -> {
				localAddress as UnixDomainSocketAddress
				HAProxyMessage(
					version, HAProxyCommand.PROXY, HAProxyProxiedProtocol.UNIX_STREAM,
					remoteAddress.path.absolutePathString(), localAddress.path.absolutePathString(), 0, 0
				)
			}
			
			else -> throw HAProxyProtocolException("Unkown Address of:${remoteAddress.javaClass.simpleName}")
		}
		
		return encode(message)
	}
	
	@Throws(IOException::class)
	fun encode(remoteAddress: Address, localAddress: Address, version: HAProxyProtocolVersion): ByteArray {
		return when (version) {
			HAProxyProtocolVersion.V1 -> {
				encode(
					HAProxyMessage(
						HAProxyProtocolVersion.V1, HAProxyCommand.PROXY,
						when (remoteAddress.type) {
							AddressType.IPV4_TCP -> HAProxyProxiedProtocol.TCP4
							AddressType.IPV6_TCP -> HAProxyProxiedProtocol.TCP6
							else -> HAProxyProxiedProtocol.UNKNOWN
						},
						remoteAddress.ipString, localAddress.ipString,
						remoteAddress.port, localAddress.port
					)
				)
				
			}
			
			HAProxyProtocolVersion.V2 -> {
				encode(
					HAProxyMessage(
						HAProxyProtocolVersion.V2, HAProxyCommand.PROXY,
						when (remoteAddress.type) {
							AddressType.IPV4_TCP -> HAProxyProxiedProtocol.TCP4
							AddressType.IPV6_TCP -> HAProxyProxiedProtocol.TCP6
							AddressType.UNIX_DOMAIN_SOCKET -> HAProxyProxiedProtocol.UNIX_STREAM
							else -> HAProxyProxiedProtocol.UNKNOWN
						},
						remoteAddress.ipString, localAddress.ipString,
						remoteAddress.port, localAddress.port
					)
				)
			}
		}
	}
	
	@Throws(IOException::class)
	fun encode(msg: HAProxyMessage): ByteArray {
		return when (msg.protocolVersion) {
			HAProxyProtocolVersion.V1 -> encodeV1(msg)
			HAProxyProtocolVersion.V2 -> encodeV2(msg)
		}
	}
	
	private const val V2_VERSION_BITMASK = 0x02 shl 4
	
	// Length for source/destination addresses for the UNIX family must be 108 bytes each.
	private const val UNIX_ADDRESS_BYTES_LENGTH = 108
	private const val TOTAL_UNIX_ADDRESS_BYTES_LENGTH = UNIX_ADDRESS_BYTES_LENGTH * 2
	
	private fun encodeV1(msg: HAProxyMessage): ByteArray {
		return ByteArrayOutputStream().writeCharSequence(
			"${V1_PREFIX.toString(Charsets.US_ASCII)} ${msg.proxiedProtocol.name}" +
				" ${msg.sourceAddress} ${msg.destinationAddress} ${msg.sourcePort} ${msg.destinationPort} \r\n"
		).toByteArray()
	}
	
	
	private fun encodeV2(msg: HAProxyMessage): ByteArray {
		return ByteArrayOutputStream().use { out: ByteArrayOutputStream ->
			out.writeBytes(V2_PREFIX)
			out.writeByte((V2_VERSION_BITMASK or msg.command.byteValue.toInt()).toByte())
			out.writeByte(msg.proxiedProtocol.byteValue)
			when (msg.proxiedProtocol.addressFamily) {
				AddressFamily.AF_IPv4, AddressFamily.AF_IPv6 -> {
					val srcAddrBytes: ByteArray = Inet6Address.getByName(msg.sourceAddress).address
					val dstAddrBytes: ByteArray = Inet6Address.getByName(msg.destinationAddress).address
					// srcAddrLen + dstAddrLen + 4 (srcPort + dstPort) + numTlvBytes
					out.writeShort((srcAddrBytes.size + dstAddrBytes.size + 4 + msg.tlvNumBytes()).toShort())
					out.writeBytes(srcAddrBytes)
					out.writeBytes(dstAddrBytes)
					out.writeShort(msg.sourcePort.toShort())
					out.writeShort(msg.destinationPort.toShort())
					encodeTlvs(msg.tlvs, out)
				}
				
				AddressFamily.AF_UNIX -> {
					out.writeShort((TOTAL_UNIX_ADDRESS_BYTES_LENGTH + msg.tlvNumBytes()).toShort())
					val bytes = ByteArrayOutputStream(UNIX_ADDRESS_BYTES_LENGTH)
					val srcBytes = msg.sourceAddress.toByteArray(Charsets.US_ASCII)
					bytes.writeBytes(srcBytes)
					bytes.writeBytes(ByteArray(UNIX_ADDRESS_BYTES_LENGTH - srcBytes.size))
					out.writeBytes(bytes.toByteArray())
					
					bytes.reset()
					val destBytes = msg.sourceAddress.toByteArray(Charsets.US_ASCII)
					bytes.writeBytes(destBytes)
					bytes.writeBytes(ByteArray(UNIX_ADDRESS_BYTES_LENGTH - destBytes.size))
					out.writeBytes(bytes.toByteArray())
					
					encodeTlvs(msg.tlvs, out)
				}
				
				AddressFamily.AF_UNSPEC -> out.writeShort(0)
			}
			out.toByteArray()
		}
	}
	
	private fun encodeTlv(haProxyTLV: HAProxyTLV, out: OutputStream) {
		if (haProxyTLV is HAProxySSLTLV) {
			out.writeByte(haProxyTLV.typeByteValue)
			out.writeShort(haProxyTLV.contentNumBytes().toShort())
			out.writeByte(haProxyTLV.client)
			out.writeInt(haProxyTLV.verify)
			encodeTlvs(haProxyTLV.tlvs, out)
		} else {
			out.writeByte(haProxyTLV.typeByteValue)
			val value = haProxyTLV.content
			val readableBytes: Int = value.size
			out.writeShort(readableBytes.toShort())
			out.writeByteArray(value)
		}
	}
	
	private fun encodeTlvs(haProxyTLVs: List<HAProxyTLV>, out: OutputStream) {
		for (i in haProxyTLVs.indices) {
			encodeTlv(haProxyTLVs[i], out)
		}
	}
}

