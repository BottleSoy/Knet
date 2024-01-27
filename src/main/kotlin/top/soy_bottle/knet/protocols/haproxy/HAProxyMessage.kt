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

import top.soy_bottle.knet.protocols.haproxy.AddressFamily.*
import top.soy_bottle.knet.utils.*
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.math.min

/**
 * Message container for decoded HAProxy proxy protocol parameters
 */
class HAProxyMessage(
	val protocolVersion: HAProxyProtocolVersion,
	val command: HAProxyCommand,
	val proxiedProtocol: HAProxyProxiedProtocol,
	val sourceAddress: String,
	val destinationAddress: String,
	val sourcePort: Int,
	val destinationPort: Int,
	val tlvs: List<HAProxyTLV> = emptyList(),
) {
	
	private constructor(
		protocolVersion: HAProxyProtocolVersion, command: HAProxyCommand, proxiedProtocol: HAProxyProxiedProtocol,
		sourceAddress: String, destinationAddress: String, sourcePort: String, destinationPort: String,
	) : this(
		protocolVersion, command, proxiedProtocol,
		sourceAddress, destinationAddress, portStringToInt(sourcePort), portStringToInt(destinationPort)
	)
	
	fun tlvNumBytes(): Int {
		var tlvNumBytes = 0
		for (i in tlvs.indices) {
			tlvNumBytes += tlvs[i].totalNumBytes()
		}
		return tlvNumBytes
	}
	
	
	override fun toString(): String {
		val sb: StringBuilder = StringBuilder(256)
			.append("HAProxyMessage")
			.append("(protocolVersion: ").append(protocolVersion)
			.append(", command: ").append(command)
			.append(", proxiedProtocol: ").append(proxiedProtocol)
			.append(", sourceAddress: ").append(sourceAddress)
			.append(", destinationAddress: ").append(destinationAddress)
			.append(", sourcePort: ").append(sourcePort)
			.append(", destinationPort: ").append(destinationPort)
			.append(", tlvs: [")
		if (tlvs.isNotEmpty()) {
			for (tlv in tlvs) {
				sb.append(tlv).append(", ")
			}
			sb.setLength(sb.length - 2)
		}
		sb.append("])")
		return sb.toString()
	}
	
	companion object {
		// Let's pick some conservative limit here.
		private const val MAX_NESTING_LEVEL = 128
		
		
		/**
		 * Decodes a version 2, binary proxy protocol header.
		 *
		 * @param input                     a version 2 proxy protocol header stream
		 * @return                           [HAProxyMessage] instance
		 * @throws HAProxyProtocolException  if any portion of the header is invalid
		 */
		fun decodeV2(input: InputStream): HAProxyMessage {
//			ObjectUtil.checkNotNull(header, "header")
			if (input.available() < 16) {
				throw HAProxyProtocolException("incomplete header: ${input.available()} bytes (expected: 16+ bytes)")
			}
			
			// Per spec, the 13th byte is the protocol version and command byte
			input.skip(12)
			val verCmdByte: Byte = input.readByte()
			val ver: HAProxyProtocolVersion = try {
				HAProxyProtocolVersion.valueOf(verCmdByte)
			} catch (e: IllegalArgumentException) {
				throw HAProxyProtocolException(e)
			}
			if (ver != HAProxyProtocolVersion.V2) {
				throw HAProxyProtocolException("version 1 unsupported: 0x" + Integer.toHexString(verCmdByte.toInt()))
			}
			val cmd: HAProxyCommand = try {
				HAProxyCommand.valueOf(verCmdByte)
			} catch (e: IllegalArgumentException) {
				throw HAProxyProtocolException(e)
			}
			if (cmd == HAProxyCommand.LOCAL) {
				return unknownMsg(HAProxyProtocolVersion.V2, HAProxyCommand.LOCAL)
			}
			
			// Per spec, the 14th byte is the protocol and address family byte
			val protAndFam: HAProxyProxiedProtocol = try {
				HAProxyProxiedProtocol.valueOf(input.readByte())
			} catch (e: IllegalArgumentException) {
				throw HAProxyProtocolException(e)
			}
			if (protAndFam === HAProxyProxiedProtocol.UNKNOWN) {
				return unknownMsg(HAProxyProtocolVersion.V2, HAProxyCommand.PROXY)
			}
			val addressInfoLen: Int = input.readUShort()
			val srcAddress: String
			val destAddress: String
			val addressLen: Int
			var srcPort = 0
			var destPort = 0
			val addressFamily = protAndFam.addressFamily
			if (addressFamily === AF_UNIX) {
				// unix sockets require 216 bytes for address information
				if (addressInfoLen < 216 || input.available() < 216) {
					throw HAProxyProtocolException(
						"incomplete UNIX socket address information: ${min(addressInfoLen, input.available())}"
							+ " bytes (expected: 216+ bytes)"
					)
				}
				val srcAddrBytes = input.readNBytes(108)
				val srcSize = srcAddrBytes.indexOf(0)
				srcAddress = srcAddrBytes.inputStream().readString(srcSize)
				val destAddrBytes = input.readNBytes(108)
				val destSize = destAddrBytes.indexOf(0)
				destAddress = destAddrBytes.inputStream().readString(destSize)
				
			} else {
				addressLen = when (addressFamily) {
					AF_IPv4 -> {
						// IPv4 requires 12 bytes for address information
						if (addressInfoLen < 12 || input.available() < 12) {
							throw HAProxyProtocolException(
								"incomplete IPv4 address information:  ${min(addressInfoLen, input.available())}"
									+ " bytes (expected: 12+ bytes)"
							)
						}
						4
					}
					
					AF_IPv6 -> {
						// IPv6 requires 36 bytes for address information
						if (addressInfoLen < 36 || input.available() < 36) {
							throw HAProxyProtocolException(
								"incomplete IPv6 address information: ${min(addressInfoLen, input.available())}" +
									" bytes (expected: 36+ bytes)"
							)
						}
						16
					}
					
					else -> {
						throw HAProxyProtocolException(
							"unable to parse address information (unknown address family: $addressFamily)"
						)
					}
				}
				
				// Per spec, the src address begins at the 17th byte
				srcAddress = ipBytesToString(input, addressLen)
				destAddress = ipBytesToString(input, addressLen)
				srcPort = input.readUShort()
				destPort = input.readUShort()
			}
			val tlvs = readTlvs(input)
			return HAProxyMessage(ver, cmd, protAndFam, srcAddress, destAddress, srcPort, destPort, tlvs)
		}
		
		private fun readTlvs(input: InputStream): List<HAProxyTLV> {
			var haProxyTLV: HAProxyTLV = readNextTLV(input, 0) ?: return emptyList()
			// In most cases there are less than 4 TLVs available
			val haProxyTLVs: MutableList<HAProxyTLV> = ArrayList(4)
			do {
				haProxyTLVs.add(haProxyTLV)
				if (haProxyTLV is HAProxySSLTLV) {
					haProxyTLVs.addAll(haProxyTLV.encapsulatedTLVs())
				}
			} while (readNextTLV(input, 0)?.also { haProxyTLV = it } != null)
			return haProxyTLVs
		}
		
		private fun readNextTLV(header: InputStream, nestingLevel: Int): HAProxyTLV? {
			if (nestingLevel > MAX_NESTING_LEVEL) {
				throw HAProxyProtocolException(
					"Maximum TLV nesting level reached: $nestingLevel (expected: < $MAX_NESTING_LEVEL)"
				)
			}
			// We need at least 4 bytes for a TLV
			if (header.available() < 4) {
				return null
			}
			val typeAsByte: Byte = header.readByte()
			val type = HAProxyTLV.Type.typeForByteValue(typeAsByte)
			val length: Int = header.readUShort()
			return when (type) {
				HAProxyTLV.Type.PP2_TYPE_SSL -> {
					val rawContent = header.readNBytes(length)
//					val rawContent: ByteBuf = header.retainedSlice(header.readerIndex(), length)
//					val byteBuf: ByteBuf = header.readSlice(length)
					val byteBuf = rawContent.inputStream()
					val client: Byte = byteBuf.readByte()
					val verify: Int = byteBuf.readInt()
					if (byteBuf.available() >= 4) {
						val encapsulatedTlvs: MutableList<HAProxyTLV> = ArrayList(4)
						do {
							val haProxyTLV = readNextTLV(byteBuf, nestingLevel + 1) ?: break
							encapsulatedTlvs.add(haProxyTLV)
						} while (byteBuf.available() >= 4)
						return HAProxySSLTLV(verify, client, encapsulatedTlvs, rawContent)
					}
					HAProxySSLTLV(verify, client, emptyList<HAProxyTLV>(), rawContent)
				}
				
				else ->
					HAProxyTLV(type, typeAsByte, header.readNBytes(length))
				
			}
		}
		
		/**
		 * Decodes a version 1, human-readable proxy protocol header.
		 *
		 * @param line                     a version 1 proxy protocol header line
		 * @return                           [HAProxyMessage] instance
		 * @throws HAProxyProtocolException  if any portion of the header is invalid
		 */
		@Throws(IOException::class)
		fun decodeV1(line: String?): HAProxyMessage {
			if (line == null) {
				throw HAProxyProtocolException("header")
			}
			val parts = line.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()
			val numParts = parts.size
			if (numParts < 2) {
				throw HAProxyProtocolException("invalid header: $line (expected: 'PROXY' and proxied protocol values)")
			}
			if ("PROXY" != parts[0]) {
				throw HAProxyProtocolException("unknown identifier: " + parts[0])
			}
			val protAndFam: HAProxyProxiedProtocol = try {
				HAProxyProxiedProtocol.valueOf(parts[1])
			} catch (e: IllegalArgumentException) {
				throw HAProxyProtocolException(e)
			}
			if (protAndFam !== HAProxyProxiedProtocol.TCP4 && protAndFam !== HAProxyProxiedProtocol.TCP6 && protAndFam !== HAProxyProxiedProtocol.UNKNOWN) {
				throw HAProxyProtocolException("unsupported v1 proxied protocol: " + parts[1])
			}
			if (protAndFam === HAProxyProxiedProtocol.UNKNOWN) {
				return unknownMsg(HAProxyProtocolVersion.V1, HAProxyCommand.PROXY)
			}
			if (numParts != 6) {
				throw HAProxyProtocolException("invalid TCP4/6 header: $line (expected: 6 parts)")
			}
			return try {
				HAProxyMessage(
					HAProxyProtocolVersion.V1, HAProxyCommand.PROXY,
					protAndFam, parts[2], parts[3], parts[4], parts[5]
				)
			} catch (e: RuntimeException) {
				throw HAProxyProtocolException("invalid HAProxy message", e)
			}
		}
		
		/**
		 * Proxy protocol message for 'UNKNOWN' proxied protocols. Per spec, when the proxied protocol is
		 * 'UNKNOWN' we must discard all other header values.
		 */
		private fun unknownMsg(version: HAProxyProtocolVersion, command: HAProxyCommand): HAProxyMessage {
			return HAProxyMessage(version, command, HAProxyProxiedProtocol.UNKNOWN, "", "", 0, 0)
		}
		
		/**
		 * Convert ip address bytes to string representation
		 *
		 * @param input     buffer containing ip address bytes
		 * @param addressLen number of bytes to read (4 bytes for IPv4, 16 bytes for IPv6)
		 * @return           string representation of the ip address
		 */
		private fun ipBytesToString(input: InputStream, addressLen: Int): String {
			val sb = StringBuilder()
			val ipv4Len = 4
			val ipv6Len = 8
			if (addressLen == ipv4Len) {
				for (i in 0 until ipv4Len) {
					sb.append(input.read() and 0xff)
					sb.append('.')
				}
			} else {
				for (i in 0 until ipv6Len) {
					sb.append(Integer.toHexString(input.readUShort()))
					sb.append(':')
				}
			}
			sb.setLength(sb.length - 1)
			return sb.toString()
		}
		
		/**
		 * Convert port to integer
		 *
		 * @param value                      the port
		 * @return                           port as an integer
		 * @throws IllegalArgumentException  if port is not a valid integer
		 */
		private fun portStringToInt(value: String): Int {
			val port: Int = try {
				value.toInt()
			} catch (e: NumberFormatException) {
				throw IllegalArgumentException("invalid port: $value", e)
			}
			require(!(port <= 0 || port > 65535)) { "invalid port: $value (expected: 1 ~ 65535)" }
			return port
		}
		
		/**
		 * Validate an address (IPv4, IPv6, Unix Socket)
		 *
		 * @param address                    human-readable address
		 * @param addrFamily                 the [AddressFamily] to check the address against
		 * @throws IllegalArgumentException  if the address is invalid
		 */
		private fun checkAddress(address: String, addrFamily: AddressFamily) {
			when (addrFamily) {
				AF_UNSPEC -> {
//					require(false) { "unable to validate an AF_UNSPEC address: $address" }
					return
				}
				
				AF_UNIX -> {
					require(address.toByteArray(Charsets.US_ASCII).size <= 108) { "invalid AF_UNIX address: $address" }
					return
				}
				
				AF_IPv4 -> require(address.isValidIPV4()) { "invalid IPv4 address: $address" }
				AF_IPv6 -> require(address.isValidIPV6()) { "invalid IPv6 address: $address" }
			}
		}
		
		/**
		 * Validate the port depending on the addrFamily.
		 *
		 * @param port                       the UDP/TCP port
		 * @throws IllegalArgumentException  if the port is out of range (0-65535 inclusive)
		 */
		private fun checkPort(port: Int, addrFamily: AddressFamily) {
			when (addrFamily) {
				AF_IPv6, AF_IPv4 -> require(port in 0..65535) { "invalid port: $port (expected: 0 ~ 65535)" }
				AF_UNIX, AF_UNSPEC -> require(port == 0) { "port cannot be specified with addrFamily: $addrFamily" }
			}
		}
	}
}


