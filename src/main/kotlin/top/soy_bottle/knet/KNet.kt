package top.soy_bottle.knet

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.io.FileSystem
import org.graalvm.polyglot.io.IOAccess
import top.soy_bottle.knet.KNet.Mode.*
import top.soy_bottle.knet.client.KNetClient
import top.soy_bottle.knet.config.JSConfig
import top.soy_bottle.knet.config.KTSConfig
import top.soy_bottle.knet.server.KNetServer
import java.io.File
import java.io.OutputStream
import javax.script.ScriptEngineManager

object KNet {
	enum class Mode {
		CLIENT, SERVER;
	}
	
	lateinit var app: KApplication
	
	@JvmStatic
	fun main(args: Array<String>) {
		var at = 0;
		var configFile = File("config.js")
		var mode = SERVER
		while (at < args.size) {
			when (args[at++]) {
				//自定义配置文件路径
				"-c" -> configFile = File(args[at++])
				"-m" -> mode = Mode.valueOf(args[at++])
				else -> break
			}
		}
		
		val jsContext = Context.newBuilder("js")

			.logHandler(OutputStream.nullOutputStream())
			.build()
		jsContext.getBindings("js").putMember("mode", mode)
		val res = jsContext.eval(Source.newBuilder("js", configFile)
			.name(configFile.name)
			.build())
		val config = JSConfig(res)
		
		
		app = when (mode) {
			CLIENT -> KNetClient(config)
			SERVER -> KNetServer(config)
		}
		app.start()
		
	}
}