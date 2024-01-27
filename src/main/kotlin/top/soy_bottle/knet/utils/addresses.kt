package top.soy_bottle.knet.utils

import java.net.Inet6Address
import java.util.regex.Pattern

private val IPV4_PATTERN = Pattern.compile(
	"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
		"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
)

fun String.isValidIPV4(): Boolean {
	return IPV4_PATTERN.matcher(this).matches()
}

private val IPV6_PATTERN = Pattern.compile(
	"^([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]){1,4}$" +
		"|^(([0-9a-fA-F]{1,4}:){1,7}|:):" +
		"|^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$" +
		"|^([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}$" +
		"|^([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}$" +
		"|^([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}$" +
		"|^([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}$" +
		"|^[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})$" +
		"|^:((:[0-9a-fA-F]{1,4}){1,7}|:)$"
)

fun String.isValidIPV6(): Boolean {
	return IPV6_PATTERN.matcher(this).matches()
}
