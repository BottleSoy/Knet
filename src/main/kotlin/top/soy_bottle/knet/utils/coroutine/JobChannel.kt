package top.soy_bottle.knet.utils.coroutine

import java.util.ArrayDeque
import java.util.Queue

class JobChannel<R : Any> {
	val datas = ArrayDeque<R>()
	val waitingThread: Thread? = null
	fun send(value: R) {
		datas.addFirst(value)
		waitingThread?.interrupt()
	}
	
	fun receive(): R {
		return synchronized(datas) {
			if (datas.isEmpty()) {
				Thread.sleep(Long.MAX_VALUE)
			}
			datas.removeLast()
		}
	}
}