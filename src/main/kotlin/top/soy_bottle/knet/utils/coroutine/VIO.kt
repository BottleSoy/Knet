package top.soy_bottle.knet.utils.coroutine
//
//import kotlinx.coroutines.CoroutineDispatcher
//import kotlinx.coroutines.Runnable
//import top.soy_bottle.knet.logger
//import java.util.concurrent.ForkJoinPool
//import java.util.concurrent.ThreadFactory
//import java.util.concurrent.atomic.AtomicInteger
//import kotlin.concurrent.thread
//import kotlin.coroutines.CoroutineContext
//
///**
// * 虚拟线程执行器
// */
//object VIO : CoroutineDispatcher() {
//	private var userLandThread: Thread? = null
//	val count = AtomicInteger(0)
//	private var c = 0
//	val virtualThreads = HashSet<Thread>()
//	override fun dispatch(context: CoroutineContext, block: Runnable) {
//		virtualThreads += Thread.ofPlatform().name("VIO-${c++}").start {
//			logger.info("{VIO} started:${Thread.currentThread().name}")
//			count.incrementAndGet()
//			block.run()
//			count.decrementAndGet()
//			if (count.get() == 0 && userLandThread != null) {
//				userLandThread!!.interrupt()
//			}
//			virtualThreads -= Thread.currentThread()
//		}
//		logger.info("{VIO} created VIO-${c}")
////		println("vthread dump at${System.currentTimeMillis()}:" + virtualThreads.map { it.stackTrace.asList() })
//	}
//
//	fun setDaemon(daemon: Boolean) {
//		if (daemon) {
//			userLandThread?.interrupt()
//		} else {
//			if (userLandThread == null) {
//				userLandThread = thread(name = "VIO hangup thread") {
//					runCatching {
//						while (true)
//							Thread.sleep(Long.MAX_VALUE)
//					}
//				}
//			}
//		}
//	}
//}