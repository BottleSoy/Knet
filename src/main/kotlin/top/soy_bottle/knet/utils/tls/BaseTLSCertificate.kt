package top.soy_bottle.knet.utils.tls

import top.soy_bottle.knet.utils.chain
import java.security.PrivateKey
import java.security.cert.X509Certificate

class BaseTLSCertificate(
	override val privateKey: PrivateKey,
	certs: List<X509Certificate>,
) : TLSCertificate {
	override val certificates: List<X509Certificate> =
		certs
//			.chain(head = { it.issuerUniqueID }, tail = { it.subjectUniqueID })!!.toList()
}