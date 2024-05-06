package top.soy_bottle.knet

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalTime

val logger: Logger = LoggerFactory.getLogger("KNet").apply {

}

interface KApplication {
	
	fun start()
}