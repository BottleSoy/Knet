/*
 * Copyright 2016 The Netty Project
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

import java.util.*

/**
 * Represents a [HAProxyTLV] of the type [HAProxyTLV.Type.PP2_TYPE_SSL].
 * This TLV encapsulates other TLVs and has additional information like verification information and a client bitfield.
 */
class HAProxySSLTLV internal constructor(
	val verify: Int,
	val client: Byte,
	val tlvs: List<HAProxyTLV>,
	rawContent: ByteArray,
) : HAProxyTLV(
	Type.PP2_TYPE_SSL, 0x20.toByte(), rawContent
) {
	
	/**
	 * Creates a new HAProxySSLTLV
	 *
	 * @param verify         the verification result as defined in the specification for the pp2_tlv_ssl struct (see
	 * [...](https://www.haproxy.org/download/1.8/doc/proxy-protocol.txt))
	 * @param clientBitField the bitfield with client information
	 * @param tlvs           the encapsulated [HAProxyTLV]s
	 */
	constructor(verify: Int, clientBitField: Byte, tlvs: List<HAProxyTLV>) : this(
		verify,
		clientBitField,
		tlvs,
		ByteArray(0)
	)
	
	/**
	 * Creates a new HAProxySSLTLV
	 *
	 * @param verify         the verification result as defined in the specification for the pp2_tlv_ssl struct (see
	 * [...](https://www.haproxy.org/download/1.8/doc/proxy-protocol.txt))
	 * @param clientBitField the bitfield with client information
	 * @param tlvs           the encapsulated [HAProxyTLV]s
	 * @param rawContent     the raw TLV content
	 */
	
	
	val isPP2ClientCertConn: Boolean
		/**
		 * Returns `true` if the bit field for PP2_CLIENT_CERT_CONN was set
		 */
		get() = client.toInt() and 0x2 != 0
	val isPP2ClientSSL: Boolean
		/**
		 * Returns `true` if the bit field for PP2_CLIENT_SSL was set
		 */
		get() = client.toInt() and 0x1 != 0
	val isPP2ClientCertSess: Boolean
		/**
		 * Returns `true` if the bit field for PP2_CLIENT_CERT_SESS was set
		 */
		get() = client.toInt() and 0x4 != 0
	
	/**
	 * Returns the client bit field
	 */
	fun client(): Byte {
		return client
	}
	
	/**
	 * Returns the verification result
	 */
	fun verify(): Int {
		return verify
	}
	
	/**
	 * Returns an unmodifiable Set of encapsulated [HAProxyTLV]s.
	 */
	fun encapsulatedTLVs(): List<HAProxyTLV> {
		return tlvs
	}
	
	override fun contentNumBytes(): Int {
		var tlvNumBytes = 0
		for (tlv in tlvs) {
			tlvNumBytes += tlv.totalNumBytes()
		}
		return 5 + tlvNumBytes // clientBit(1) + verify(4) + tlvs
	}
}
