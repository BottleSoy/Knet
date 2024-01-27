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

class ConnectionInfo(
	val realAddress: Address,
	val remoteAddress: Address,
	val localAddress: Address,
	val protocolNames: List<String>,
)

@Synchronized

@Throws(IOException::class)
fun <O : OutputStream> O.writeConnectionInfo(info: ConnectionInfo): O {
	this.writeAddress(info.realAddress)
	this.writeAddress(info.remoteAddress)
	this.writeAddress(info.localAddress)
	this.writeStringList(info.protocolNames)
	return this
}

@Synchronized

@Throws(IOException::class)
fun InputStream.readConnectionInfo(): ConnectionInfo {
	return ConnectionInfo(readAddress(), readAddress(), readAddress(), readStringList())
}