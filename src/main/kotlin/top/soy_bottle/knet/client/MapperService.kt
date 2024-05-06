package top.soy_bottle.knet.client

import top.soy_bottle.knet.mappers.ClientMapper
import top.soy_bottle.knet.mappers.MapperType
import top.soy_bottle.knet.mappers.packet.ClientMappers
import top.soy_bottle.knet.mappers.tcp.TcpMapperType
import top.soy_bottle.knet.mappers.udp.UdpMapperType
import top.soy_bottle.knet.utils.*

@Env(Scope.Client)
class MapperService(val client: KNetClient) {
	val mappers: ArrayList<ClientMapper> = ArrayList()
	val mapperTypes: HashMap<String, MapperType> = HashMap()
	
	init {
		mapperTypes["tcp"] = TcpMapperType
		mapperTypes["udp"] = UdpMapperType
	}
	
	operator fun get(mapperId: Int) = mappers[mapperId]
	fun createMappersPacket(): ClientMappers {
		return ClientMappers(mappers.map {
			ClientMappers.MapperMsg(it.id, it.type, it.createClientMsg())
		})
	}
}
