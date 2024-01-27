package top.soy_bottle.knet.config

interface SectionConfig : Iterable<String> {
	
	fun keyTypes(): Map<String, Class<*>>
	
	operator fun get(key: String): Any?
	
	fun getString(key: String): String
	
	fun getBoolean(key: String): Boolean
	
	fun getInt(key: String): Int
	
	fun getDouble(key: String): Double
	
	fun getSub(key: String): SectionConfig
	
	fun <R> getFunction(key: String): () -> R
	
	fun <R> getFunction(key: String, vararg params: List<Class<*>>): (List<*>) -> R
	
	fun getList(key: String): ListConfig
	
	
}

fun SectionConfig.getStringOrEmpty(key: String) = runCatching {
	getString(key)
}.getOrDefault("")

fun SectionConfig.getStringOrNull(key: String) = runCatching {
	getString(key)
}.getOrDefault(null)

fun SectionConfig.getOrDefault(key: String, default: String) = runCatching {
	getString(key)
}.getOrDefault(default)

fun SectionConfig.getOrDefault(key: String, default: Boolean) = runCatching {
	getBoolean(key)
}.getOrDefault(default)

fun SectionConfig.getOrDefault(key: String, default: Int) = runCatching {
	getInt(key)
}.getOrDefault(default)

fun SectionConfig.getOrDefault(key: String, default: Double) = runCatching {
	getDouble(key)
}.getOrDefault(default)

fun <R> SectionConfig.getOrDefault(key: String, default: () -> R) = runCatching {
	getFunction<R>(key)
}.getOrDefault(default)


fun SectionConfig.getSubOrNull(key: String): SectionConfig? = this[key] as? SectionConfig

fun SectionConfig.getListOrNull(key: String): ListConfig? = this[key] as? ListConfig
