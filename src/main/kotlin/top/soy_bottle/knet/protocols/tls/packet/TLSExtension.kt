package top.soy_bottle.knet.protocols.tls.packet

import top.soy_bottle.knet.utils.readByteArray
import top.soy_bottle.knet.utils.readShort
import java.io.IOException
import java.io.InputStream

data class TLSExtension(val type: TLSExtensionType, val typeID: Short, val size: Short, val data: ByteArray) {
	enum class TLSExtensionType(val typeID: Short) {
		SERVER_NAME(0),
		MAX_FRAGMENT_LENGTH(1),
		CLIENT_CERTIFICATE_URL(2),
		TRUSTED_CA_KEYS(3),
		TRUNCATED_HMAC(4),
		STATUS_REQUEST(5),
		USER_MAPPING(6),
		CLIENT_AUTHZ(7),
		SERVER_AUTHZ(8),
		CERT_TYPE(9),
		SUPPORTED_GROUPS(10),
		EC_POINT_FORMATS(11),
		SRP(12),
		SIGNATURE_ALGORITHMS(13),
		USE_SRTP(14),
		HEARTBEAT(15),
		APPLICATION_LAYER_PROTOCOL_NEGOTIATION(16),
		STATUS_REQUEST_V2(17),
		SIGNED_CERTIFICATE_TIMESTAMP(18),
		CLIENT_CERTIFICATE_TYPE(19),
		SERVER_CERTIFICATE_TYPE(20),
		PADDING(21),
		ENCRYPT_THEN_MAC(22),
		EXTENDED_MASTER_SECRET(23),
		TOKEN_BINDING(24),
		CACHED_INFO(25),
		TLS_LTS(26),
		COMPRESS_CERTIFICATE(27),
		RECORD_SIZE_LIMIT(28),
		PWD_PROTECT(29),
		PWD_CLEAR(30),
		PASSWORD_SALT(31),
		TICKET_PINNING(32),
		TLS_CERT_WITH_EXTERN_PSK(33),
		DELEGATED_CREDENTIAL(34),
		SESSION_TICKET(35),
		TLMSP(36),
		TLMSP_PROXYING(37),
		TLMSP_DELEGATE(38),
		SUPPORTED_EKT_CIPHERS(39),
		PRE_SHARED_KEY(41),
		EARLY_DATA(42),
		SUPPORTED_VERSIONS(43),
		COOKIE(44),
		PSK_KEY_EXCHANGE_MODES(45),
		RESERVED(46),
		CERTIFICATE_AUTHORITIES(47),
		OID_FILTERS(48),
		POST_HANDSHAKE_AUTH(49),
		SIGNATURE_ALGORITHMS_CERT(50),
		KEY_SHARE(51),
		TRANSPARENCY_INFO(52),
		DEPRECATED_CONNECTION_ID(53),
		CONNECTION_ID(54),
		EXTERNAL_ID_HASH(55),
		EXTERNAL_SESSION_ID(56),
		QUIC_TRANSPORT_PARAMETERS(57),
		TICKET_REQUEST(58),
		DNSSEC_CHAIN(59),
		SEQUENCE_NUMBER_ENCRYPTION_ALGORITHMS(60),
		RRC(61),
		RENEGOTIATION_INFO(65281.toShort()),
		UNKNOWN(-1);
	}
	
	
	companion object {
		val types: Map<Short, TLSExtensionType> = buildMap(TLSExtensionType.values().size) {
			TLSExtensionType.values().forEach {
				this[it.typeID] = it
			}
		}
		
		fun read(extendLen: Short, i: InputStream): List<TLSExtension> {
			var remains = extendLen.toInt()
			println("TLSExtension Len:$remains")
			val extensions = arrayListOf<TLSExtension>()
			while (remains > 0) {
				val typeID = i.readShort()
				val type = types[typeID] ?: TLSExtensionType.UNKNOWN
				val size = i.readShort()
				if (size > remains) throw IOException("Extension Size:$size >Extension remains Len:$remains")
				extensions += TLSExtension(type, typeID, size, i.readByteArray(size.toInt()))
				remains -= (size + 4)
			}
			if (remains != 0) {
				throw IOException("Extension remains Len:$remains, excpecting 0")
			}
			return extensions
		}
	}
}