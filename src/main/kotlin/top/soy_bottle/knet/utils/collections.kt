package top.soy_bottle.knet.utils

import java.util.*

fun <T, V> Collection<T>.chain(head: (T) -> V, tail: (T) -> V): Queue<T>? {
	val headToValue: MutableMap<V, T> = HashMap()
	val tailToValue: MutableMap<V, T> = HashMap()
	for (t in this) {
		val headValue: V = head(t)
		val tailValue: V = tail(t)
		if (headToValue.containsKey(headValue) || tailToValue.containsKey(tailValue)) {
			return null // The set cannot be chained.
		}
		headToValue[headValue] = t
		tailToValue[tailValue] = t
	}
	
	val result: Queue<T> = LinkedList()
	for (t in this) {
		if (!tailToValue.containsKey(head(t))) {
			var current: T? = t
			while (current != null) {
				result.add(current)
				current = headToValue[tail(current)]
			}
			break
		}
	}
	return if (result.size == this.size) result else null
}
