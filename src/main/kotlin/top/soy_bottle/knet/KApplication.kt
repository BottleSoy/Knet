package top.soy_bottle.knet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger("KNet")

interface KApplication {
	
	fun start()
}