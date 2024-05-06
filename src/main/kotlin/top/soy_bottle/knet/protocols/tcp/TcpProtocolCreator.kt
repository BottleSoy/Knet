package top.soy_bottle.knet.protocols.tcp

import top.soy_bottle.knet.address.toAddress
import top.soy_bottle.knet.config.JSFunction
import top.soy_bottle.knet.config.SectionConfig
import top.soy_bottle.knet.protocols.ProtocolCreator
import top.soy_bottle.knet.protocols.selector.ProtocolSelector
import java.net.InetSocketAddress
import java.net.SocketAddress

object TcpProtocolCreator : ProtocolCreator<TcpProtocol> {
	override fun createProtocol(config: SectionConfig): Result<TcpProtocol> {
		return runCatching {
			val name = config.getString("name")
			val address = when (val addr = config["address"]) {
				is JSFunction -> addr.invoke<SocketAddress>()
				is String -> addr.toAddress()
				is Int -> InetSocketAddress(addr)
				else -> throw IllegalArgumentException()
			}!!
			val selector = ProtocolSelector.of(config["selector"])!!
			TcpProtocol(name, TcpProtocolConfig(address, selector))
		}
	}
}
