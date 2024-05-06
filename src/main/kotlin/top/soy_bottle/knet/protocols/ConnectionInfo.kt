package top.soy_bottle.knet.protocols

import top.soy_bottle.knet.address.Address
import top.soy_bottle.knet.address.readAddress
import top.soy_bottle.knet.address.writeAddress
import top.soy_bottle.knet.utils.readStringList
import top.soy_bottle.knet.utils.writeString
import top.soy_bottle.knet.utils.writeStringList
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable

class ConnectionInfo(
	val realAddress: Address,
	val remoteAddress: Address,
	val localAddress: Address,
	val protocolNames: List<String>,
) : Serializable

@Throws(IOException::class)
fun <O : OutputStream> O.writeConnectionInfo(info: ConnectionInfo) = synchronized(this) {
	this.writeAddress(info.realAddress)
		.writeAddress(info.remoteAddress)
		.writeAddress(info.localAddress)
		.writeStringList(info.protocolNames)
}

@Throws(IOException::class)
fun InputStream.readConnectionInfo(): ConnectionInfo = synchronized(this) {
	return ConnectionInfo(
		readAddress(),
		readAddress(),
		readAddress(),
		readStringList()
	)
}