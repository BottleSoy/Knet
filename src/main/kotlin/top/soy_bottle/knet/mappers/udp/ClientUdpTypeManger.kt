package top.soy_bottle.knet.mappers.udp

import top.soy_bottle.knet.client.KNetClient
import top.soy_bottle.knet.mappers.TypeManager

class ClientUdpTypeManger(val client: KNetClient) : TypeManager {
	val connections = hashMapOf<Int, ClientUdpEndpoint>()
	
	
}