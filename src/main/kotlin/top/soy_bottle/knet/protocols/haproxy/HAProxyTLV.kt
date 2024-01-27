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

/**
 * A Type-Length Value (TLV vector) that can be added to the PROXY protocol
 * to include additional information like SSL information.
 *
 * @see HAProxySSLTLV
 */
open class HAProxyTLV internal constructor(
	val type: Type,
	val typeByteValue: Byte,
	val content: ByteArray,
) {
	
	/**
	 * Creates a new HAProxyTLV
	 *
	 * @param typeByteValue the byteValue of the TLV. This is especially important if non-standard TLVs are used
	 * @param content       the raw content of the TLV
	 */
	constructor(typeByteValue: Byte, content: ByteArray) : this(
		Type.typeForByteValue(typeByteValue),
		typeByteValue,
		content
	)
	
	/**
	 * Creates a new HAProxyTLV
	 *
	 * @param type    the [Type] of the TLV
	 * @param content the raw content of the TLV
	 */
	constructor(type: Type, content: ByteArray) : this(type, Type.byteValueForType(type), content)
	
	
	/**
	 * The size of this tlv in bytes.
	 *
	 * @return the number of bytes.
	 */
	fun totalNumBytes(): Int {
		return 3 + contentNumBytes() // type(1) + length(2) + content
	}
	
	open fun contentNumBytes(): Int {
		return content.size
	}
	
	override fun toString(): String {
		return "HAProxyTLV(type=$type, content=${content.contentToString()})"
	}
	
	/**
	 * The registered types a TLV can have regarding the PROXY protocol 1.5 spec
	 */
	enum class Type {
		PP2_TYPE_ALPN,
		PP2_TYPE_AUTHORITY,
		PP2_TYPE_SSL,
		PP2_TYPE_SSL_VERSION,
		PP2_TYPE_SSL_CN,
		PP2_TYPE_NETNS,
		
		/**
		 * A TLV type that is not officially defined in the spec. May be used for nonstandard TLVs
		 */
		OTHER;
		
		companion object {
			/**
			 * Returns the [Type] for a specific byte value as defined in the PROXY protocol 1.5 spec
			 *
			 *
			 * If the byte value is not an official one, it will return [Type.OTHER].
			 *
			 * @param byteValue the byte for a type
			 * @return the [Type] of a TLV
			 */
			fun typeForByteValue(byteValue: Byte): Type {
				return when (byteValue.toInt()) {
					0x01 -> PP2_TYPE_ALPN
					0x02 -> PP2_TYPE_AUTHORITY
					0x20 -> PP2_TYPE_SSL
					0x21 -> PP2_TYPE_SSL_VERSION
					0x22 -> PP2_TYPE_SSL_CN
					0x30 -> PP2_TYPE_NETNS
					else -> OTHER
				}
			}
			
			/**
			 * Returns the byte value for the [Type] as defined in the PROXY protocol 1.5 spec.
			 *
			 * @param type the [Type]
			 * @return the byte value of the [Type].
			 */
			fun byteValueForType(type: Type): Byte {
				return when (type) {
					PP2_TYPE_ALPN -> 0x01
					PP2_TYPE_AUTHORITY -> 0x02
					PP2_TYPE_SSL -> 0x20
					PP2_TYPE_SSL_VERSION -> 0x21
					PP2_TYPE_SSL_CN -> 0x22
					PP2_TYPE_NETNS -> 0x30
					else -> throw IllegalArgumentException("unknown type: $type")
				}
			}
		}
	}
	
	
}
