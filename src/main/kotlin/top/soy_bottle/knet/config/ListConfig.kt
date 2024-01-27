package top.soy_bottle.knet.config

interface ListConfig : Iterable<Any> {
	val size: Int
	operator fun get(index: Int): Any
}

fun ListConfig.doubleList(): List<Double> {
	val list = ArrayList<Double>(size)
	repeat(size) {
		list.add(get(it) as Double)
	}
	return list
}

fun ListConfig.intList(): List<Int> {
	val list = ArrayList<Int>(size)
	repeat(size) {
		list.add(get(it) as Int)
	}
	return list
}

fun ListConfig.stringList(): List<String> {
	val list = ArrayList<String>(size)
	repeat(size) {
		list.add(get(it) as String)
	}
	return list
}

fun ListConfig.sectionList(): List<SectionConfig> {
	val list = ArrayList<SectionConfig>(size)
	repeat(size) {
		list.add(get(it) as SectionConfig)
	}
	return list
}

