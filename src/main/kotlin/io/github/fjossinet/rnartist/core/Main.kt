package io.github.fjossinet.rnartist.core

import java.io.File
import java.io.FileReader
import java.nio.file.Paths
import javax.script.ScriptEngineManager

class Jar() {
    fun path() = Paths.get(Jar::class.java.getProtectionDomain().getCodeSource().getLocation()
            .toURI()).parent.toAbsolutePath()
}

fun main(args:Array<String>) {
    if (args.isEmpty()) {
        println("""
Usage: java -jar rnartistcore-X.X.X-jar-with-dependencies.jar [-c] path/to/your/plotting_script.kts
""")
        System.exit(-1)
    }
    val manager = ScriptEngineManager()
    val engine = manager.getEngineByExtension("kts")
    engine.eval(
        "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
            System.getProperty(
                "line.separator"
            )
        } ${FileReader(File(args.last())).readText()}")
}

