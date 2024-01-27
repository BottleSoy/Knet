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

import top.soy_bottle.knet.protocols.haproxy.HAProxyConstants.VERSION_ONE_BYTE
import top.soy_bottle.knet.protocols.haproxy.HAProxyConstants.VERSION_TWO_BYTE

/**
 * The HAProxy proxy protocol specification version.
 */
enum class HAProxyProtocolVersion
/**
 * Creates a new instance
 */(private val byteValue: Byte) {
	/**
	 * The ONE proxy protocol version represents a version 1 (human-readable) header.
	 */
	V1(VERSION_ONE_BYTE),
	
	/**
	 * The TWO proxy protocol version represents a version 2 (binary) header.
	 */
	V2(VERSION_TWO_BYTE);
	
	/**
	 * Returns the byte value of this version.
	 */
	fun byteValue(): Byte {
		return byteValue
	}
	
	companion object {
		/**
		 * The highest 4 bits of the protocol version and command byte contain the version
		 */
		private const val VERSION_MASK = 0xf0.toByte()
		
		/**
		 * Returns the [HAProxyProtocolVersion] represented by the highest 4 bits of the specified byte.
		 *
		 * @param verCmdByte protocol version and command byte
		 */
		fun valueOf(verCmdByte: Byte): HAProxyProtocolVersion {
			val version = verCmdByte.toInt() and VERSION_MASK.toInt()
			return when (version.toByte()) {
				VERSION_TWO_BYTE -> V2
				VERSION_ONE_BYTE -> V1
				else -> throw IllegalArgumentException("unknown version: $version")
			}
		}
	}
}
