package top.soy_bottle.knet.config

import org.graalvm.polyglot.Value
import top.soy_bottle.knet.KNet
import top.soy_bottle.knet.config.JSConfig.Companion.parseToRaw
import java.time.LocalDate
import java.time.LocalTime

class JSFunction(val value: Value) {
	fun invokeRaw(vararg params: Any?): Any? = value.execute(*params).parseToRaw()
	inline fun <reified T : Any> invoke(vararg params: Any?): T? {
		/**
		 *
		 */
		return synchronized(KNet) {
			val v = try {
				val va = value.execute(*params)
				va.parseToRaw()
			} catch (e: Exception) {
				e.printStackTrace()
				null
			} ?: return null
			when (T::class) {
				String::class -> v as T
				Boolean::class -> v as T
				Int::class -> v as T
				Double::class -> v as T
				LocalDate::class -> v as T
				LocalTime::class -> v as T
				else -> {
					v as JSConfig
					v.value.`as`(T::class.java)
				}
			}
		}
		
	}
	
}
