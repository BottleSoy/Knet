package top.soy_bottle.knet.utils.coroutine

private var bCount = 0
class BasicJob(name: String = "job-${bCount++}",run: () -> Unit) {
	val jobThread = Thread.ofPlatform().name(name).start {
		run()
	}
	
	fun await() {
		jobThread.join()
	}
	
}
private var aCount = 0
class AsyncJob<R : Any?>(name: String = "asyncjob-${aCount++}", run: () -> R) {
	var result: R? = null
	var done = false
	
	val jobThread = Thread.ofVirtual().name(name).start {
		val res = run()
		result = res
		done = true
	}
	
	fun await(): R {
		jobThread.join()
		if (done)
			return result!!
		else
			throw IllegalStateException("R not returned!!!")
	}
}