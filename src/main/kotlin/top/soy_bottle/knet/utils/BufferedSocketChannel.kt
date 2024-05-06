package top.soy_bottle.knet.utils

import java.net.Socket
import java.net.SocketAddress
import java.net.SocketOption
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.SocketChannel

class BufferedSocketChannel(
	val socketChannel: SocketChannel
) {

	val remoteAddress: SocketAddress get() = socketChannel.remoteAddress
}