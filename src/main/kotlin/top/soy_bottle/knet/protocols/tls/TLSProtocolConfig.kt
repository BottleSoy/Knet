package top.soy_bottle.knet.protocols.tls

import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.protocols.selector.ProtocolSelector
import top.soy_bottle.knet.protocols.tls.packet.TLSClientHello
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLParameters

data class TLSProtocolConfig(
	/**
	 * 根据目标条件创建SSLContext对象
	 */
	val createContext: (connection: Connection, hello: TLSClientHello) -> SSLContext?,
	/**
	 * 对于即将成为TLS连接的连接，配置TLS
	 */
	val configureSSLParameters: (connection: Connection, hello: TLSClientHello, context: SSLContext, param: SSLParameters) -> SSLParameters,
	
	val selectProtocol: (connection: TLSConnection) -> ProtocolSelector,
)