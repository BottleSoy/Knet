package top.soy_bottle.knet.mappers

import top.soy_bottle.knet.client.KNetClient
import top.soy_bottle.knet.config.KNetConfig

/**
 * 内网穿透类型
 *
 */
interface MapperType {
	val name: String
	
	/**
	 * 创建客户端映射器
	 * @param client 客户端
	 * @param config 映射配置
	 * @param id 客户端创建的唯一ID
	 */
	fun createClientMapper(
		client: KNetClient,
		config: KNetConfig,
		id: Int
	): ClientMapper
	
	/**
	 * 创建服务端的映射器
	 *
	 * @param server 服务端
	 * @param client 客户端连接
	 * @param id 客户端创建的唯一ID
	 * @param msg 客户端创建的字节流数据，数据由[ClientMapper.createClientMsg]创建
	 */
	fun createServerMapper(
		client: MapperClientConnection,
		id: Int,
		msg: ByteArray,
	): ServerMapper
	
}