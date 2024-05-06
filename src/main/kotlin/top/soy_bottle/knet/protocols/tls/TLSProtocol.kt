package top.soy_bottle.knet.protocols.tls

import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.protocols.tls.packet.TLSClientHello
import top.soy_bottle.knet.utils.SizeLimitedInputStream
import top.soy_bottle.knet.utils.markWith
import top.soy_bottle.knet.utils.tls.defaultSSLContext
import top.soy_bottle.knet.utils.withTimeout
import java.io.IOException
import java.nio.charset.Charset
import javax.net.ssl.SNIMatcher
import javax.net.ssl.SNIServerName

class TLSProtocol(override val name: String, val config: TLSProtocolConfig) : AbstractProtocol<TLSConnection> {
	override val type: String = "TLS"
	override val connections = hashSetOf<TLSConnection>()
	
	override fun detect(connection: Connection) = detectIsSSL(connection.input)
	
	@Throws(IOException::class)
	override fun directHandle(connection: Connection) {
		val hello = withTimeout(3000) { //设置ClientHello读取时间
			connection.input.markWith(16384) {
				TLSClientHello.fromStream(this)
			}
		}
		val context = this.config.createContext(connection, hello)
		if (context != null) {
			connections += TLSConnection(this, connection, hello, context.socketFactory, {
				this.config.configureSSLParameters(connection, hello, context, it)
			}) { e, c ->
				try {
					val target = this.config.selectProtocol(c)
					target.selectAndHandle(c)
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		} else { //空Context,返回不匹配任何ServerName。
			connections += TLSConnection(this, connection, hello, defaultSSLContext.socketFactory, {
				it.sniMatchers = listOf(object : SNIMatcher(0) {
					override fun matches(serverName: SNIServerName?) = false
				})
//				it.protocols = arrayOf()
//				it.cipherSuites = arrayOf()
				it.serverNames = listOf()
				/*
				 * 在这里配置的SSLParameters会反应不同的客户端错误
				 * 比如说不配置任何protocol则会让客户端不知道TLS具体版本
				 * 不配置任何cipherSuites则会让客户端无法使用加密算法
				 */
				it
			}) { _, c ->
				c.close()
			}
		}
	}
	
	
	companion object {
		
		/**
		 * 获取ClientHello的前11位消息
		 *
		 * 在这个方法中，读取了首包的11个字节
		 *
		 * **TLS ClientHello前11位消息如下:**
		 *
		 * - `0`:  Content Type，固定为0x16
		 * - `1`, `2`: Version，指定TLS版本,TLS v1.0是`0x03 0x01`,越往后越大
		 * - `3`, `4`: Length，TLS包长度(忽略)
		 * - `5`: Handshake Type，在ClientHello中固定为`0x01`
		 * - `6`, `7`, `8`: Handshake Length，TLS 握手包长度(忽略)
		 * - `9`, `10`: Client Version，客户端的TLS版本
		 *
		 * 	[这里有详细说明TLS ClientHello包](https://tls12.xargs.org/#client-hello/annotated)
		 */
		fun detectIsSSL(input: SizeLimitedInputStream): Boolean {
			input.markWith(11) {
				val msg = ByteArray(11)
				val size = input.read(msg, 0, msg.size)
				if (size < 11) return false
				return msg[0] == 0x16.toByte() &&
					msg[1] == 0x03.toByte() &&
					msg[2] >= 0x01.toByte() &&
					msg[5] == 0x01.toByte() &&
					msg[6] == 0x00.toByte() &&
					msg[9] == 0x03.toByte() &&
					msg[10] >= 0x01.toByte()
			}
			
		}
	}
}