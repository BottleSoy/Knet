package top.soy_bottle.knet.utils.tls

import top.soy_bottle.knet.utils.toLocalDateTime
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.time.LocalDateTime
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

/**
 * TLS域名证书
 */
interface TLSCertificate {
	/**
	 * TLS域名证书私钥
	 */
	val privateKey: PrivateKey
	
	/**
	 * TLS证书公钥链
	 */
	val certificates: List<X509Certificate>
	
	/**
	 * 证书服务的域名
	 */
	val domains
		get() = certificates.first().subjectAlternativeNames.map {
			it[1] as String
		}
	
	/**
	 * 证书有效时间区域
	 */
	val vaildTime: ClosedRange<LocalDateTime>
		get() = certificates.first().let {
			it.notBefore.toLocalDateTime()..it.notAfter.toLocalDateTime()
		}
}


/**
 * 验证域名是否可以兼容。
 */
fun TLSCertificate.isCompatible(domain: String) = domains.any {
	isDomainMatched(it, domain)
}

fun TLSCertificate.createTLSContext(protocol: String = "TLS"): SSLContext {
	val keystore = KeyStore.getInstance("JKS")
	keystore.load(null)
	keystore.setCertificateEntry("alias", certificates.first())
	
	keystore.setKeyEntry("alias", privateKey, "password".toCharArray(), certificates.toTypedArray())
	
	val kmf = KeyManagerFactory.getInstance("SunX509")
	kmf.init(keystore, "password".toCharArray())
	val context = SSLContext.getInstance(protocol)
	context.init(kmf.keyManagers, null, null)
	return context
}

fun isDomainMatched(certificateDomain: String, domainToCheck: String): Boolean {
	val certs = certificateDomain.split(".")
	val checks = domainToCheck.split(".")
	if (certs.size == checks.size) {
		var done = true
		for (i in certs.indices) {
			if (certs[i] == "*")
				continue
			if (certs[i] != checks[i]) {
				done = false
				break;
			}
		}
		return done
	}
	return false
}
