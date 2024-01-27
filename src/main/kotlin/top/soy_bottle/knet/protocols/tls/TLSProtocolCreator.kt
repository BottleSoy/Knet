package top.soy_bottle.knet.protocols.tls

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERNull
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.pkcs.RSAPrivateKey
import org.bouncycastle.asn1.sec.ECPrivateKey
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x509.DSAParameter
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.openssl.PKCS8Generator
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import top.soy_bottle.knet.config.*
import top.soy_bottle.knet.protocols.Connection
import top.soy_bottle.knet.protocols.ProtocolCreator
import top.soy_bottle.knet.protocols.selector.DirectProtocolSelector
import top.soy_bottle.knet.protocols.selector.EmptyProtocolSelector
import top.soy_bottle.knet.protocols.selector.JSProtocolSelector
import top.soy_bottle.knet.protocols.selector.ProtocolSelector
import top.soy_bottle.knet.protocols.tls.packet.TLSClientHello
import top.soy_bottle.knet.protocols.tls.packet.getSNI
import top.soy_bottle.knet.utils.tls.*
import java.io.File
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLParameters


object TLSProtocolCreator : ProtocolCreator<TLSProtocol> {
	override fun createProtocol(config: SectionConfig): Result<TLSProtocol> = runCatching {
		val name = config.getString("name")
		var contextCreator: (connection: Connection, hello: TLSClientHello) -> SSLContext? = { _, _ -> null }
		if (config.keyTypes().containsKey("context")) {
			contextCreator = { _, _ -> getContext(config["context"]) }
		} else if (config.keyTypes().containsKey("contexts")) {
			when (val contexts = config["contexts"]) {
				is ListConfig -> {
					val certs = mutableListOf<TLSCertificate>()
					contexts.sectionList().forEach { section ->
						val context = getCertificate(section["pemFiles"]) ?: return@forEach
						certs.add(context)
					}
					val certContexts = mapOf(*certs.map { it to it.createTLSContext() }.toTypedArray())
					
					when (val contextSelector = config["context-selector"] ?: "perDomain") {
						is String -> when (contextSelector) {
							"perDomain" -> contextCreator =
								object : (Connection, TLSClientHello) -> SSLContext? {
									override fun invoke(con: Connection, hello: TLSClientHello): SSLContext? {
										val sni = hello.getSNI() ?: return null
										return certContexts[certs.firstOrNull {
											val r = it.isCompatible(sni)
											r
										}]
									}
								}
							
							"perDomainAndLastFallback" -> {
								object : (Connection, TLSClientHello) -> SSLContext? {
									val lastContext = certContexts[certs.last()]
									override fun invoke(con: Connection, hello: TLSClientHello) = run {
										val sni = hello.getSNI() ?: return@run null
										certContexts[certs.firstOrNull { it.isCompatible(sni) }]
									} ?: lastContext
								}
							}
						}
						
						is JSFunction -> contextCreator = { c, h -> certContexts[certs[contextSelector(c, h) as Int]] }
					}
				}
				
				is JSFunction -> contextCreator = { c, h -> contexts.invoke(c, h) as SSLContext? }
			}
		}
		var sslConfigure: (Connection, TLSClientHello, SSLContext, SSLParameters) -> SSLParameters =
			{ _, _, _, d -> d }
		when (val configure = config["configure"]) {
			is JSFunction -> sslConfigure = { c, h, t, d -> configure.invoke(c, h, t, d) as SSLParameters }
			is SectionConfig -> {
				val tlsVersion = configure.getStringOrStringListOrNull("tls-version")?.toTypedArray()
				val alpn = configure.getStringOrStringListOrNull("alpn")?.toTypedArray()
				val chipers = configure.getStringOrStringListOrNull("chipers")?.toTypedArray()
				
				sslConfigure = { c, h, t, s ->
					tlsVersion?.let { s.protocols = it }
					alpn?.let { s.applicationProtocols = it }
					chipers?.let { s.cipherSuites = chipers }
					s
				}
			}
		}
		val protocolSelector: (TLSConnection) -> ProtocolSelector =
			when (val selector = config["selector"]) {
				is JSFunction -> { _ ->
					JSProtocolSelector(selector)
				}
				
				is SectionConfig -> {
					val domains = LinkedHashMap<String, ProtocolSelector>()
					selector.getSubOrNull("domains")?.let {
						it.forEach { key ->
							domains[key] = ProtocolSelector.of(it[key]) ?: EmptyProtocolSelector
						}
					}
					val fallbackSelector = ProtocolSelector.of(selector["fallback"]) ?: EmptyProtocolSelector
					select@{
						val sni = it.helloPacket.getSNI()
						if (sni != null) {
							domains.forEach { (domain, select) ->
								if (isDomainMatched(domain, sni)) {
									return@select select
								}
							}
						}
						fallbackSelector
					}
				}
				
				is String -> { _ ->
					DirectProtocolSelector(selector)
				}
				
				else -> { _ -> EmptyProtocolSelector }
			}
		TLSProtocol(name, TLSProtocolConfig(contextCreator, sslConfigure, protocolSelector))
	}
	
	private fun getCertificate(data: Any?): TLSCertificate? = when (data) {
		null -> null
		is JSFunction -> data() as TLSCertificate?
		is ListConfig -> {
			val pems = arrayListOf<PemObject>()
			data.stringList().forEach {
				val reader = PemReader(File(it).reader())
				do {
					val pem = reader.readPemObject()?.apply {
						pems += this
					}
				} while (pem != null)
			}
			var privateKey: PrivateKey? = null
			val certificates = arrayListOf<X509Certificate>()
			
			pems.forEach { pem ->
				when (pem.type) {
					"RSA PRIVATE KEY" -> {
						val factory = KeyFactory.getInstance("RSA")
						val obj = RSAPrivateKey.getInstance(ASN1Sequence.getInstance(pem.content));
						val algId = AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE)
						val keybytes = PKCS8Generator(PrivateKeyInfo(algId, obj), null).generate().content
						val spec = PKCS8EncodedKeySpec(keybytes)
						privateKey = factory.generatePrivate(spec)
					}
					
					"EC PRIVATE KEY" -> {
						val factory = KeyFactory.getInstance("EC")
						val obj = ECPrivateKey.getInstance(ASN1Sequence.getInstance(pem.content));
						val algId = AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, obj.parametersObject)
						val keybytes = PKCS8Generator(PrivateKeyInfo(algId, obj), null).generate().content
						val spec = PKCS8EncodedKeySpec(keybytes)
						privateKey = factory.generatePrivate(spec)
					}
					
					"DSA PRIVATE KEY" -> {
						val factory = KeyFactory.getInstance("DSA")
						val seq = ASN1Sequence.getInstance(pem.content);
						val p = ASN1Integer.getInstance(seq.getObjectAt(1))
						val q = ASN1Integer.getInstance(seq.getObjectAt(2))
						val g = ASN1Integer.getInstance(seq.getObjectAt(3))
						val y = ASN1Integer.getInstance(seq.getObjectAt(4))
						val x = ASN1Integer.getInstance(seq.getObjectAt(5))
						
						
						val keybytes = PKCS8Generator(
							PrivateKeyInfo(
								AlgorithmIdentifier(
									X9ObjectIdentifiers.id_dsa,
									DSAParameter(p.value, q.value, g.value)
								), x
							), null
						).generate().content
						val spec = PKCS8EncodedKeySpec(keybytes)
						privateKey = factory.generatePrivate(spec)
					}
					
					"X509 CERTIFICATE", "CERTIFICATE" -> {
						val x509Factory = CertificateFactory.getInstance("X.509")
						x509Factory.generateCertificates(pem.content.inputStream()).forEach {
							certificates.add(it as X509Certificate)
						}
					}
				}
			}
			privateKey?.let { BaseTLSCertificate(it, certificates) }
		}

//		is SectionConfig -> {
//
//		}
		
		else -> throw IllegalArgumentException("data is not a viald js type!!!")
	}
	
	val defaultContext = SSLContext.getDefault()
	
	private fun getContext(data: Any?): SSLContext? {
		return when (data) {
			null -> defaultContext
			is JSFunction -> (data() as SSLContext)
			
			is SectionConfig -> {
				val cert = getCertificate(data["pemFiles"]) ?: return null
				return cert.createTLSContext()
			}
			
			else -> throw IllegalArgumentException("data is not a viald js type!!!")
		}
	}
}

class SSLContextEnv(
	val context: SSLContext,
	val cert: TLSCertificate?,
)

fun SectionConfig.getStringOrStringListOrNull(key: String): List<String>? {
	when (val v = this[key]) {
		is String -> return arrayListOf(v)
		is ListConfig -> return v.stringList()
	}
	return null
}