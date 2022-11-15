package io.github.fjossinet.rnartist.core

import io.github.fjossinet.rnartist.core.io.parseDSLScript
import java.io.File
import java.io.FileReader
import java.io.StringWriter
import java.nio.file.Paths
import javax.script.ScriptEngineManager

class Jar() {
    fun path() = Paths.get(Jar::class.java.getProtectionDomain().getCodeSource().getLocation()
            .toURI()).parent.toAbsolutePath()
}

fun main(args:Array<String>) {
    if (args.isEmpty()) {
        println("""
Usage: java -jar rnartist_core.jar [-c] dsl_script_file
    Options:
        -c : check the syntax of the script before to run it. Print issues found.
""")
        System.exit(-1)
    }
    if (args.contains("-c")) {
        val (elements, issues) = parseDSLScript(FileReader(File(args.last())))
        if (issues.isNotEmpty()) {
            println("Issues found in the script")
            issues.forEach {
                println(it)
            }
        }
        val writer = StringWriter()
        elements.forEach {
            it.print(writer, "   ")
        }
        //println(writer)
        val manager = ScriptEngineManager()
        val engine = manager.getEngineByExtension("kts")
        engine.eval(
            "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                System.getProperty(
                    "line.separator"
                )
            } ${writer}"
        )
    } else {
        val manager = ScriptEngineManager()
        val engine = manager.getEngineByExtension("kts")
        engine.eval(
            "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                System.getProperty(
                    "line.separator"
                )
            } ${FileReader(File(args.last())).readText()}")
    }
}

