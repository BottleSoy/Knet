package top.soy_bottle.knet.config

import org.graalvm.polyglot.Value
import top.soy_bottle.knet.config.JSConfig.Companion.parseToRaw

class JSListConfig(val value: Value) : ListConfig {
	val data = Array(value.arraySize.toInt()) {
		value.getArrayElement(it.toLong()).parseToRaw()!!
	}
	
	override val size: Int = data.size
	
	override fun get(index: Int) = data[index]
	
	override fun iterator() = data.iterator()
	
}
