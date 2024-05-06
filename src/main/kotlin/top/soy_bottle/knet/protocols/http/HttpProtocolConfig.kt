package top.soy_bottle.knet.protocols.http

class HttpProtocolConfig(
	/**
	 * 是否使用http1协议(包括了http/0.9,http/1.0,http/1.1)处理连接
	 */
	val useHttp1: Boolean = true,
	/**
	 * 是否使用h2协议处理连接
	 */
	val useHttp2: Boolean = true,
	/**
	 * 对于http1/http2协议，可以对TLS协议进行注入，简化https实现
	 */
	val tlsInject: TLSInjectConfig? = null,
	
	
)

class TLSInjectConfig(
	/**
	 * 注入的TLS协议名称
	 */
	val protocolName: String,
	/**
	 * 是否添加ALPN(Application Protocol Name)标识
	 */
	val addAlpn: Boolean = true,
	/**
	 * 优先注入，即Http响应是否在TLS配置之前
	 */
	val prior: Boolean = false,
)