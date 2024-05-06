package top.soy_bottle.knet.mappers.tcp

sealed interface TcpAction

data class SendData(val data: ByteArray, val len: Int = data.size) : TcpAction

data object ReqeustClose : TcpAction
