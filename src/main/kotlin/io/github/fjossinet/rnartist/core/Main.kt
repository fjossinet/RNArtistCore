package io.github.fjossinet.rnartist.core

import java.io.File
import javax.script.ScriptEngineManager

fun main(args:Array<String>) {
    if (args.isEmpty()) {
        println("Usage: java -jar rnartist_core.jar dsl_script_file")
        System.exit(-1)
    }
    val manager = ScriptEngineManager()
    val engine = manager.getEngineByExtension("kts")
    engine.eval("import io.github.fjossinet.rnartist.core.*\n\n ${File(args[0]).readText()}")
}

