package top.soy_bottle.knet.client

import top.soy_bottle.knet.mappers.ClientMapper
import top.soy_bottle.knet.mappers.MapperType
import top.soy_bottle.knet.utils.Env
import top.soy_bottle.knet.utils.Scope

@Env(Scope.Client)
class MapperService(val client: KNetClient) {
	val mappers: List<ClientMapper>
	val mapperTypes: Map<String, MapperType>
	
	init {
		mappers = ArrayList()
		mapperTypes = HashMap()
	}
	
	fun createMapperMsg() {
	
	}
}