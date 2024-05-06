package top.soy_bottle.knet.protocols.http

import org.eclipse.jetty.server.LocalConnector
import top.soy_bottle.knet.protocols.BasicConnection
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.utils.coroutine.BasicJob
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class HttpConnection(
	connection: Connection,
	override val protocol: HttpProtocol,
	val endpoint: LocalConnector.LocalEndPoint,
) : BasicConnection(connection) {
	fun start() {
		BasicJob {
			val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
			var count = input.read(buffer)
			while (count >= 0) {
				endpoint.addInput(ByteBuffer.wrap(buffer, 0, count))
				count = input.read(buffer)
			}
		}
		BasicJob {
			var buf = endpoint.waitForOutput(Long.MAX_VALUE, TimeUnit.DAYS)
			while (buf.limit() != 0) {
				output.write(buf.array())
				output.flush()
				buf = endpoint.waitForOutput(Long.MAX_VALUE, TimeUnit.DAYS)
			}
		}
	}
	
	override fun close() {
		endpoint.close()
		super.close()
	}
}