package top.soy_bottle.knet.protocols


/**
 * 一个网络协议
 */
interface AbstractProtocol<Con : Connection> {
	/**
	 * 网络协议类型
	 */
	val type: String
	
	/**
	 * 网络协议名称
	 */
	val name: String
	
	/**
	 * 当前协议下的所有连接
	 */
	val connections: HashSet<Con>
	
	/**
	 * 检测是不是当前协议
	 *
	 * **请在检测之后将连接还原**
	 * @param connection 测试的连接
	 */
	fun detect(connection: Connection): Boolean
	
	/**
	 * 直接处理连接
	 *
	 * **有可能会产生错误**
	 *
	 * 产生错误后应该关闭连接
	 * @param connection 目标连接
	 * @throws Exception 造成连接的错误且连接内不可修复
	 */
	@Throws(Exception::class)
	fun directHandle(connection: Connection)
	
}