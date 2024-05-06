package top.soy_bottle.knet.protocols.http

import org.eclipse.jetty.server.LocalConnector
import org.eclipse.jetty.server.Server
import top.soy_bottle.knet.protocols.AbstractProtocol
import top.soy_bottle.knet.protocols.Connection

class HttpProtocol(override val name: String) : AbstractProtocol<HttpConnection> {
	override val type: String = "http"
	override val connections: HashSet<HttpConnection> = hashSetOf()
	
	val server: Server = Server()
	
	val connector = LocalConnector(server)
	
	init {
		server.addConnector(connector)
		server.start()
		
		
	}
	
	override fun detect(connection: Connection): Boolean {
		return super.detect(connection)
	}
	
	override fun directHandle(connection: Connection) {
		val endpoint = connector.connect()
		val c = HttpConnection(connection, this, endpoint)
		connections += c
		c.start()
		
	}
}