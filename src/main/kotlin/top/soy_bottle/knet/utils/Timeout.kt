package top.soy_bottle.knet.utils

import top.soy_bottle.knet.utils.coroutine.AsyncJob
/**
 * 对操作进行限时，否则将抛出[TimeoutException]
 * @param millTime 限制的时间
 * @param action 执行操作的函数
 * @param dispather 执行函数运用的执行器，默认为虚拟线程
 */
@Throws(TimeoutException::class)
fun <R> withTimeout(millTime: Int, action: () -> R): R {
	var error: TimeoutException? = null
	val mainThread = Thread.currentThread()
	val task = AsyncJob {
		val res = action()
		if (error == null) {
			mainThread.interrupt()
		}
		res
	}
	try {
		Thread.sleep(millTime.toLong())
		task.jobThread.interrupt()
		error = TimeoutException(millTime, task.jobThread.stackTrace.first())
		throw error
	} catch (e: Exception) {
		return task.await()
	}
}

class TimeoutException(val time: Int, stackTraceElement: StackTraceElement) : RuntimeException() {
	override val message: String = "could't execute in ${time}ms at $stackTraceElement"
}