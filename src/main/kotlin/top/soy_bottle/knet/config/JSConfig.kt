package top.soy_bottle.knet.config

import org.graalvm.polyglot.Value

class JSConfig(val value: Value) : KNetConfig, SectionConfig {
	val data: HashMap<String, Any?> = HashMap(value.memberKeys.size)
	
	init {
		val keys = value.memberKeys
		keys.forEach {
			val member = value.getMember(it)!!
			data[it] = member.parseToRaw()
		}
	}
	
	override fun keyTypes(): Map<String, Class<*>> = data.mapValues { (k, v) ->
		(v?.javaClass as? Class<*>) ?: Void::class.java
	}
	
	override fun get(key: String) = data[key]
	
	override fun getString(key: String) = data[key] as String
	
	override fun getBoolean(key: String) = data[key] as Boolean
	
	override fun getInt(key: String) = data[key] as Int
	
	override fun getDouble(key: String) = data[key] as Double
	
	override fun getSub(key: String) = data[key] as SectionConfig
	
	override fun <R> getFunction(key: String): () -> R = {
		(data[key] as JSFunction).invoke() as R
	}
	
	override fun <R> getFunction(key: String, vararg params: List<Class<*>>): (List<*>) -> R = {
		(data[key] as JSFunction).invoke(*it.toTypedArray()) as R
	}
	
	override fun getList(key: String) = data[key] as ListConfig
	
	override fun iterator(): Iterator<String> = data.keys.iterator()
	
	companion object {
		fun Value.parseToRaw(): Any? {
			return when {
				isString -> asString()
				isBoolean -> asBoolean()
				fitsInInt() -> asInt()
				fitsInDouble() -> asDouble()
				isDate -> asDate()
				canExecute() -> JSFunction(this)
				
				hasArrayElements() -> JSListConfig(this)
				hasMembers() -> JSConfig(this)
				isNull -> null
				
				else -> null
			}
		}
	}
}

