package top.soy_bottle.knet.protocols.tls.packet

import top.soy_bottle.knet.utils.readByte
import top.soy_bottle.knet.utils.readByteArray
import top.soy_bottle.knet.utils.readShort
import java.io.InputStream

data class TLSClientHello(
	/*...初始头 开始...*/
	val handshake: Byte, //[0]
	val protocol: Short, //[1,2]
	val len: Short, //[3,4]
	/*...初始头 结束...*/
	val handshakeType: Byte, //[5]
	val handshakeLen: Short, //[6,7,8]
	val clientProtocol: Short, //[9,10]
	val random: ByteArray, //[11..42]
	val sessionID: ByteArray, //sessionID
	val chiperData: ByteArray,
	val compressionData: ByteArray,
	val extends: List<TLSExtension>,
) {
	companion object {
		fun fromStream(input: InputStream): TLSClientHello {
			return TLSClientHello(
				handshake = input.readByte().apply {
					if (this != 0x16.toByte()) throw IllegalArgumentException("Invaild TLS hello packet!!!")
				},
				protocol = input.readShort().apply {
					if (this < 0x0300) throw IllegalArgumentException("Invaild TLS hello packet!!!")
				},
				len = input.readShort().apply {
					if (input.available() < this) throw IllegalArgumentException("Invaild TLS hello packet!!!")
				},
				handshakeType = input.readByte().apply {
					if (this != 0x01.toByte()) throw IllegalArgumentException("Invaild TLS hello packet!!!")
				},
				handshakeLen = run {
					input.readByte().apply {//第六位==0
						if (this != 0x00.toByte()) throw IllegalArgumentException("Invaild TLS hello packet!!!")
					}
					input.readShort().apply {
						
						if (input.available() < this) throw IllegalArgumentException("Invaild TLS hello packet!!!")
					}
				},
				clientProtocol = input.readShort().apply {
					if (this < 0x0300) throw IllegalArgumentException("Invaild TLS hello packet!!!")
				},
				random = input.readByteArray(32),
				sessionID = input.readByteArray(input.readByte().toInt()),
				chiperData = input.readByteArray(input.readShort().toInt()),
				compressionData = input.readByteArray(input.readByte().toInt()),
				
				extends = TLSExtension.read(input.readShort(), input)
			)
		}
		
		fun fromBytes(array: ByteArray) = fromStream(array.inputStream())
	}
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		
		other as TLSClientHello
		
		if (handshake != other.handshake) return false
		if (protocol != other.protocol) return false
		if (len != other.len) return false
		if (handshakeType != other.handshakeType) return false
		if (handshakeLen != other.handshakeLen) return false
		if (clientProtocol != other.clientProtocol) return false
		if (!random.contentEquals(other.random)) return false
		if (!sessionID.contentEquals(other.sessionID)) return false
		if (!chiperData.contentEquals(other.chiperData)) return false
		if (!compressionData.contentEquals(other.compressionData)) return false
		if (extends != other.extends) return false
		
		return true
	}
	
	override fun hashCode(): Int {
		var result = handshake.toInt()
		result = 31 * result + protocol
		result = 31 * result + len
		result = 31 * result + handshakeType
		result = 31 * result + handshakeLen
		result = 31 * result + clientProtocol
		result = 31 * result + random.contentHashCode()
		result = 31 * result + sessionID.contentHashCode()
		result = 31 * result + chiperData.contentHashCode()
		result = 31 * result + compressionData.contentHashCode()
		result = 31 * result + extends.hashCode()
		return result
	}
}

fun TLSClientHello.getSNI(): String? {
	val extension = this.extends.find { it.type == TLSExtension.TLSExtensionType.SERVER_NAME } ?: return null
	val datai = extension.data.inputStream()
	datai.skip(2) //sni len
	if (datai.readByte() == 0.toByte()) {
		val hostData = datai.readByteArray(datai.readShort().toInt())
		return hostData.decodeToString()
	} else {
		return null
	}
}