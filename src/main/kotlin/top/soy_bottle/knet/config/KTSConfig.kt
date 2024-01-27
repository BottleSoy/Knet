package top.soy_bottle.knet.config

import java.io.File
import javax.script.ScriptEngine

class KTSConfig(val ktsFile: File, val engine: ScriptEngine) : KNetConfig {
	fun execute() {
		engine.eval(ktsFile.reader())
	}
}