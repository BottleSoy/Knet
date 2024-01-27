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

internal object HAProxyConstants {
	/**
	 * Command byte constants
	 */
	const val COMMAND_LOCAL_BYTE: Byte = 0x00
	const val COMMAND_PROXY_BYTE: Byte = 0x01
	
	/**
	 * Version byte constants
	 */
	const val VERSION_ONE_BYTE: Byte = 0x10
	const val VERSION_TWO_BYTE: Byte = 0x20
	
	/**
	 * Transport protocol byte constants
	 */
	const val TRANSPORT_UNSPEC_BYTE: Byte = 0x00
	const val TRANSPORT_STREAM_BYTE: Byte = 0x01
	const val TRANSPORT_DGRAM_BYTE: Byte = 0x02
	
	/**
	 * Address family byte constants
	 */
	const val AF_UNSPEC_BYTE: Byte = 0x00
	const val AF_IPV4_BYTE: Byte = 0x10
	const val AF_IPV6_BYTE: Byte = 0x20
	const val AF_UNIX_BYTE: Byte = 0x30
	
	/**
	 * Transport protocol and address family byte constants
	 */
	const val TPAF_UNKNOWN_BYTE: Byte = 0x00
	const val TPAF_TCP4_BYTE: Byte = 0x11
	const val TPAF_TCP6_BYTE: Byte = 0x21
	const val TPAF_UDP4_BYTE: Byte = 0x12
	const val TPAF_UDP6_BYTE: Byte = 0x22
	const val TPAF_UNIX_STREAM_BYTE: Byte = 0x31
	const val TPAF_UNIX_DGRAM_BYTE: Byte = 0x32
	
	/**
	 * V2 protocol binary header prefix
	 */
	val V2_PREFIX = byteArrayOf(
		0x0D, 0x0A, 0x0D, 0x0A, 0x00, 0x0D,
		0x0A, 0x51, 0x55, 0x49, 0x54, 0x0A
	)
	/**
	 * V1 protocol header prefix
	 */
	val V1_PREFIX = "PROXY".toByteArray()
}
