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
 * The command of an HAProxy proxy protocol header
 */
enum class HAProxyCommand(val byteValue: Byte) {
	/**
	 * The LOCAL command represents a connection that was established on purpose by the proxy
	 * without being relayed.
	 */
	LOCAL(HAProxyConstants.COMMAND_LOCAL_BYTE),
	
	/**
	 * The PROXY command represents a connection that was established on behalf of another node,
	 * and reflects the original connection endpoints.
	 */
	PROXY(HAProxyConstants.COMMAND_PROXY_BYTE);
	
	
	companion object {
		/**
		 * The command is specified in the lowest 4 bits of the protocol version and command byte
		 */
		private const val COMMAND_MASK: Byte = 0x0f
		
		/**
		 * Returns the [HAProxyCommand] represented by the lowest 4 bits of the specified byte.
		 *
		 * @param verCmdByte protocol version and command byte
		 */
		fun valueOf(verCmdByte: Byte): HAProxyCommand {
			val cmd = verCmdByte.toInt() and COMMAND_MASK.toInt()
			return when (cmd.toByte()) {
				HAProxyConstants.COMMAND_PROXY_BYTE -> PROXY
				HAProxyConstants.COMMAND_LOCAL_BYTE -> LOCAL
				else -> throw IllegalArgumentException("unknown command: $cmd")
			}
		}
	}
}
