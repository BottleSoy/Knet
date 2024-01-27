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

import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.protocols.haproxy.HAProxyConstants.V2_PREFIX
import top.soy_bottle.knet.protocols.haproxy.HAProxyConstants.V1_PREFIX
import top.soy_bottle.knet.protocols.selector.ProtocolSelector
import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.markWith
import top.soy_bottle.knet.utils.readUShort
import java.io.InputStream
import java.util.*

/**
 * Decodes an HAProxy proxy protocol header
 *
 * [Proxy Protocol Specification](https://haproxy.1wt.eu/download/1.5/doc/proxy-protocol.txt)
 */
class HAProxyMessageProtocol(override val name: String, val config: HAProxyProtocolConfig) :
	AbstractProtocol<HAProxyConnection> {
	override val connections = hashSetOf<HAProxyConnection>()
	override val type = "HAProxy"
	
	
	override fun detect(connection: Connection): Boolean {
		if (config.decodeV2 && detectV2(connection))
			return true
		
		if (config.decodeV1 && detectV1(connection))
			return true
		return false
	}
	
	fun detectV1(connection: Connection): Boolean {
		connection.input.markWith(5) {
			if (readNBytes(5).contentEquals(V1_PREFIX))
				return true
		}
		return false
	}
	
	fun detectV2(connection: Connection): Boolean {
		connection.input.markWith(12) {
			if (readNBytes(12).contentEquals(V2_PREFIX))
				return true
		}
		return false
	}
	@Throws(Exception::class)
	override fun directHandle(connection: Connection) {
		var c: HAProxyConnection? = null
		if (detectV2(connection)) {
			c = HAProxyConnection(this, connection, HAProxyMessage.decodeV2(connection.input))
		}
		if (detectV1(connection)) {
			c = HAProxyConnection(this, connection, HAProxyMessage.decodeV1(connection.input.bufferedReader().readLine()))
		}
		c ?: throw HAProxyProtocolException("Invaild DATA!!!")
		
		connections += c
		config.selector.selectAndHandle(c)
	}
}
