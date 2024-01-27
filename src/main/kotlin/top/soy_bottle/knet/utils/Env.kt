package top.soy_bottle.knet.utils

import kotlin.reflect.KClass

annotation class Env(
	val value: Scope,
)

enum class Scope {
	Client, Server, Both;
}

annotation class Writer(
	val clazz: KClass<*>,
	val method: String
)