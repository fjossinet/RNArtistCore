package io.github.fjossinet.rnartist.core

import io.github.fjossinet.rnartist.core.io.getScriptForDataFile
import io.github.fjossinet.rnartist.core.model.RNArtistDB
import io.github.fjossinet.rnartist.core.model.RNArtistEl
import io.github.fjossinet.rnartist.core.model.SecondaryStructureDrawing
import java.io.File
import java.io.FileFilter
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Paths
import javax.script.ScriptEngineManager
import kotlin.io.path.invariantSeparatorsPathString

val usage = """
RNArtistCore: a kotlin DSL to create and plot RNA 2D structures

Usage:
* to run a single RNArtistCore script: 
    java -jar rnartistcore-X.X.X-jar-with-dependencies.jar /path/to/your/script
* to compute the 2D plot for a single structural file: 
    java -jar rnartistcore-X.X.X-jar-with-dependencies.jar [options] -f /path/to/your/structural_file
* to compute the 2D plots for several structural files:  
    java -jar rnartistcore-X.X.X-jar-with-dependencies.jar [options] -d /path/to/the/root_folder/

Options:
-d <arg>                Compute the 2D plots from structural files stored in subfolders inside 
                        the root folder given as argument. If some files have already been processed, they will be ignored.
-f <arg>                Compute the 2D plot for a single structural file
--no-png                The kotlin scripts created for each structural file will not 
                        export the 2D plots in PNG files. This option should not be used to 
                        create a database fully compliant with the graphical tool RNArtist 
                        RNArtist needs PNG files to preview the 2Ds.
--with-svg              The kotlin scripts created for each structural file will export 
                        the 2D plots in SVG files
--from <arg>            Restart the computation of 2D plots from the file whose name without suffix 
                        is given as argument (if file named my_rna_67.vienna, then you need to type --from my_rna_67).
                        If some files have already been processed after this start file, they will be recomputed.
--min-color <arg>       Define the first color for the gradient color. The gradient color is used to 
                        incorporate quantitative values into 2D plots (default: lightyellow)
--max-color <arg>       Define the last color for the gradient color. The gradient color is used to 
                        incorporate quantitative values into 2D plots (default: firebrick)
--min-value [<arg>]     Define the min value to be used to compute the gradient color between 
                        min-color and max-color (default: 0.0)
--max-value [<arg>]     Define the max value to be used to compute the gradient color between 
                        min-color and max-color (default: 1.0)
-h                      Display help information
""".trimIndent()

class Jar() {
    fun path() = Paths.get(
        Jar::class.java.getProtectionDomain().getCodeSource().getLocation()
            .toURI()
    ).parent.invariantSeparatorsPathString
}

suspend fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println(usage)
        System.exit(-1)
    }
    val manager = ScriptEngineManager()
    val engine = manager.getEngineByExtension("kts")
    val options = args.filter { it.startsWith("-") }
    if (options.isEmpty()) {
        println("Run script ${args.last()}")
        val text = FileReader(File(args.last())).readText()
        println("Script loaded...")
        engine.eval(
            "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                System.getProperty(
                    "line.separator"
                )
            } ${text}"
        )
    }
    else
        if (options.contains("-h"))
            println(usage)
        else if (options.contains("-f")) {
            val dataFile = File(args.get(args.indexOf("-f")+1))
            var scriptDataFile = getScriptForDataFile(
                dataFile,
                dataFile.parentFile,
                args.contains("--no-png"),
                args.contains("--with-svg"),
                if (args.contains("--min-color"))
                    args.get(args.indexOf("--min-color")+1)
                else
                    "lightyellow",
                if (args.contains("--min-value"))
                    args.get(args.indexOf("--min-value")+1).toDouble()
                else
                    0.0,
                if (args.contains("--max-color"))
                    args.get(args.indexOf("--max-color")+1)
                else
                    "firebrick",
                if (args.contains("--max-value"))
                    args.get(args.indexOf("--max-value")+1).toDouble()
                else
                    1.0
            )
            if (scriptDataFile.exists()) {
                println("The script ${scriptDataFile.name} already exists. Do you agree to overwrite it? [y/N]")
                if (!"y".equals(readLine()?.trim()))
                    System.exit(-1)
                scriptDataFile.delete()
            }
            scriptDataFile = getScriptForDataFile(
                dataFile,
                dataFile.parentFile,
                args.contains("--no-png"),
                args.contains("--with-svg"),
                if (args.contains("--min-color"))
                    args.get(args.indexOf("--min-color")+1)
                else
                    "lightyellow",
                if (args.contains("--min-value"))
                    args.get(args.indexOf("--min-value")+1).toDouble()
                else
                    0.0,
                if (args.contains("--max-color"))
                    args.get(args.indexOf("--max-color")+1)
                else
                    "firebrick",
                if (args.contains("--max-value"))
                    args.get(args.indexOf("--max-value")+1).toDouble()
                else
                    1.0
            )
            println("Drawing 2D for ${scriptDataFile.name.split(".kts").first()}")
            (engine.eval(
                "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                    System.getProperty(
                        "line.separator"
                    )
                } ${FileReader(scriptDataFile).readText()}"
            ) as? Pair<List<SecondaryStructureDrawing>, RNArtistEl>)?.let {
                //we overwrite the initial script without any layout with the best layout found and stored in the DSL elements tree
                scriptDataFile.writeText(it.second.dump("", StringBuffer()).toString())
            }
        }
        else if (options.contains("-d")) {
            if (args.contains("--from")) {
                val db = RNArtistDB(args.get(args.indexOf("-d")+1))
                var foundStart = false
                db.indexedDirs.forEach { it ->
                    println("Processing folder $it")
                    val dataDir = File(it)
                    dataDir.listFiles(FileFilter {
                        it.name.endsWith(".vienna") || it.name.endsWith(".bpseq") || it.name.endsWith(".ct") || it.name.endsWith(
                            ".pdb"
                        )
                    }).forEach { dataFile ->
                        if (!foundStart)
                            foundStart = dataFile.name.startsWith(args.get(args.indexOf("--from") + 1))
                        if (foundStart) {
                            var scriptDataFile = db.getScriptForDataFile(
                                dataFile,
                                args.contains("--no-png"),
                                args.contains("--with-svg"),
                                if (args.contains("--min-color"))
                                    args.get(args.indexOf("--min-color")+1)
                                else
                                    "lightyellow",
                                if (args.contains("--min-value"))
                                    args.get(args.indexOf("--min-value")+1).toDouble()
                                else
                                    0.0,
                                if (args.contains("--max-color"))
                                    args.get(args.indexOf("--max-color")+1)
                                else
                                    "firebrick",
                                if (args.contains("--max-value"))
                                    args.get(args.indexOf("--max-value")+1).toDouble()
                                else
                                    1.0
                            )
                            //we restart from here and if a script exists we delete it to be sure to have someting clean
                            if (scriptDataFile.exists())
                                scriptDataFile.delete()
                            scriptDataFile = db.getScriptForDataFile(
                                dataFile,
                                args.contains("--no-png"),
                                args.contains("--with-svg"),
                                if (args.contains("--min-color"))
                                    args.get(args.indexOf("--min-color")+1)
                                else
                                    "lightyellow",
                                if (args.contains("--min-value"))
                                    args.get(args.indexOf("--min-value")+1).toDouble()
                                else
                                    0.0,
                                if (args.contains("--max-color"))
                                    args.get(args.indexOf("--max-color")+1)
                                else
                                    "firebrick",
                                if (args.contains("--max-value"))
                                    args.get(args.indexOf("--max-value")+1).toDouble()
                                else
                                    1.0
                            )
                            println("Drawing 2D for ${scriptDataFile.name.split(".kts").first()}")
                            (engine.eval(
                                "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                                    System.getProperty(
                                        "line.separator"
                                    )
                                } ${FileReader(scriptDataFile).readText()}"
                            ) as? Pair<List<SecondaryStructureDrawing>, RNArtistEl>)?.let {
                                //we overwrite the initial script without any layout with the best layout found and stored in the DSL elements tree
                                scriptDataFile.writeText(it.second.dump("", StringBuffer()).toString())
                            }
                        }
                    }
                }
            } else { //we create the DB
                val db = RNArtistDB(args.get(args.indexOf("-d")+1))
                //the database is indexed and the scripts for the data files are created now to be able to print the progress of the database construction
                //otherwise all the 2Ds for each data dir will be generated at once during the evaluation of the script for the data dir
                db.indexDatabase(true, true, args.contains("--no-png"),  args.contains("--with-svg"))
                db.indexedDirs.forEach { it ->
                    println("Processing folder $it")
                    val dataDir = File(it)
                    dataDir.listFiles(FileFilter {
                        it.name.endsWith(".vienna") || it.name.endsWith(".bpseq") || it.name.endsWith(".ct") || it.name.endsWith(
                            ".pdb"
                        )
                    }).forEach { dataFile ->
                        var scriptDataFile = db.getScriptForDataFile(
                            dataFile,
                            args.contains("--no-png"),
                            args.contains("--with-svg"),
                            if (args.contains("--min-color"))
                                args.get(args.indexOf("--min-color")+1)
                            else
                                "lightyellow",
                            if (args.contains("--min-value"))
                                args.get(args.indexOf("--min-value")+1).toDouble()
                            else
                                0.0,
                            if (args.contains("--max-color"))
                                args.get(args.indexOf("--max-color")+1)
                            else
                                "firebrick",
                            if (args.contains("--max-value"))
                                args.get(args.indexOf("--max-value")+1).toDouble()
                            else
                                1.0
                        )
                        if (!args.contains("--no-png") && !db.getPreviewForDataFile(scriptDataFile).exists() || args.contains("--with-svg") && !db.getSVGForDataFile(scriptDataFile).exists()) {
                            //the user asked for something that doesn't exist for the current structural file. We need to run the script
                            if (scriptDataFile.exists())//if a script already exists, we delete it to be sure to have someting clean (we could have a script without the SVG export and the user asked now for one)
                                scriptDataFile.delete()
                            scriptDataFile = db.getScriptForDataFile(
                                dataFile,
                                args.contains("--no-png"),
                                args.contains("--with-svg"),
                                if (args.contains("--min-color"))
                                    args.get(args.indexOf("--min-color")+1)
                                else
                                    "lightyellow",
                                if (args.contains("--min-value"))
                                    args.get(args.indexOf("--min-value")+1).toDouble()
                                else
                                    0.0,
                                if (args.contains("--max-color"))
                                    args.get(args.indexOf("--max-color")+1)
                                else
                                    "firebrick",
                                if (args.contains("--max-value"))
                                    args.get(args.indexOf("--max-value")+1).toDouble()
                                else
                                    1.0
                            )
                            println("Drawing 2D for ${scriptDataFile.name.split(".kts").first()}")
                            (engine.eval(
                                "import io.github.fjossinet.rnartist.core.*${System.getProperty("line.separator")}${
                                    System.getProperty(
                                        "line.separator"
                                    )
                                } ${FileReader(scriptDataFile).readText()}"
                            ) as? Pair<List<SecondaryStructureDrawing>, RNArtistEl>)?.let {
                                //we overwrite the initial script without any layout with the best layout found and stored in the DSL elements tree
                                scriptDataFile.writeText(it.second.dump("", StringBuffer()).toString())
                            }
                        }
                    }
                }
            }

        }
}

