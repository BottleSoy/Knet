package top.soy_bottle.knet.protocols.tls.packet

import top.soy_bottle.knet.utils.readByteArray
import top.soy_bottle.knet.utils.readShort
import java.io.BufferedInputStream
import java.io.InputStream

data class TLSExtension(val type: TLSExtensionType, val data: ByteArray) {
	enum class TLSExtensionType(val typeID: Short) {
		SERVER_NAME(0),
		MAX_FRAGMENT_LENGTH(1),
		CLIENT_CERTIFICATE_URL(2),
		TRUSTED_CA_KEYS(3),
		TRUNCATED_HMAC(4),
		STATUS_REQUEST(5),
		SUPPORTED_GROUPS(10),
		EC_POINT_FORMATS(11),
		SIGNATURE_ALGORITHMS(13),
		APPLICATION_LAYER_PROTOCOL_NEGOTIATION(16),
		SIGNED_CERTIFICATE_TIMESTAMP(18),
		EXTENDED_MASTER_SECRET(23),
		SESSIONTICKET(35),
		RENEGOTIATION_INFO(65281.toShort());
	}
	
	
	companion object {
		val types: Map<Short, TLSExtensionType> = buildMap(TLSExtensionType.values().size) {
			TLSExtensionType.values().forEach {
				this[it.typeID] = it
			}
		}
		
		fun read(extendLen: Short, i: InputStream): List<TLSExtension> {
			var remains = extendLen.toInt()
			val extensions = arrayListOf<TLSExtension>()
			while (remains > 0) {
				val type = types[i.readShort()]
				val size = i.readShort()
				type?.let { TLSExtension(it, i.readByteArray(size.toInt())) }?.let(extensions::add)
				remains -= (size + 4)
			}
			if (remains != 0) {
				//Extesions len != null
			}
			return extensions
		}
	}
}