package top.soy_bottle.knet.mappers

abstract class ClientMapper(val type: String, val id: Int) {
	
	abstract val name: String
	
	//创建向服务端发送的初始化数据
	abstract fun createClientMsg(): ByteArray

}