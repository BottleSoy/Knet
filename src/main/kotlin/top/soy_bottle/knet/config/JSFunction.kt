package top.soy_bottle.knet.config

import org.graalvm.polyglot.Value
import top.soy_bottle.knet.config.JSConfig.Companion.parseToRaw

class JSFunction(val value: Value) {
	operator fun invoke(vararg params: Any?): Any? = value.execute(params).parseToRaw()
}
